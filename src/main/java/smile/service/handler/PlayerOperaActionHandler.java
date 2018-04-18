package smile.service.handler;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.json.JsonUtils;
import smile.database.domain.UserEntity;
import smile.database.domain.UserFighting;
import smile.database.dto.*;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.protocol.impl.ResultDatagram;
import smile.service.home.Home;
import smile.service.home.HomeInfo;
import smile.service.home.Player;
import smile.service.home.Poker;
import smile.service.poker.*;
import smile.tool.GameHelper;
import smile.tool.IOC;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @Package: smile.service.handler
 * @Description:
 * @date: 2018/4/16 下午11:58
 * @author: liuxin
 */
@SmileComponent
@Action
public class PlayerOperaActionHandler extends AbstractActionHandler {
    private static Logger logger= LoggerFactory.getLogger(PlayerOperaActionHandler.class);

    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;

    @SubOperation(sub = 12)
    public SocketPackage operator(SocketPackage socketPackage,Channel channel){
        OperatorC2S_DTO datagram = (OperatorC2S_DTO) socketPackage.getDatagram();
        if (datagram.getPokerAsList() != null) {
            cards(socketPackage, channel);
        } else {
            opera(socketPackage, channel);
        }
        return socketPackage;
    }

    @SubOperation(sub = 10)
    public SocketPackage ready(SocketPackage socketPackage, Channel channel) {
        PlayerReadyC2S_DTO playerReadyC2SDto = (PlayerReadyC2S_DTO) socketPackage.getDatagram();
        String uid = playerReadyC2SDto.getUid();
        String hid = playerReadyC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player player = home.getPlayer(uid);
        //设置玩家为准备状态
        player.setStatus("2");
        home.updatePlayer(player);
        home.addReady();
        PlayerStatusS2C_DTO playStatus = PlayerStatusS2C_DTO.builder().chairId(player.getChairId()).status(player.getStatus())
                .uid(uid).build();
        socketPackage.setProtocol(new Protocol(2, 10));
        socketPackage.setDatagram(playStatus);
        List<Player> players = home.getPlayers();
        //将当前玩家状态，通知给其他玩家
        for (int j = 0; j < players.size(); j++) {
            Player player2 = players.get(j);
            Channel otherChannel = player2.getChannel();
            otherChannel.writeAndFlush(socketPackage);
        }
        //当前玩家准备好,判断其他玩家是否也都已经准备,当已经准备时候发牌
        if (home.isStart()) {
            Poker poker = home.getPoker();
            poker.pokerShuffle();
            poker.deal(players);
            Random random = new Random();
            //地主座位号
            int chairId = random.nextInt(players.size());
            chairId = Integer.parseInt(home.getChairdByUid("640325"));
            //设置房主座位号
//            home.setNextOperaChairId(chairId);
            home.setLandLordChairId(chairId);
            /**
             * 通知地主操作
             */
            logger.info("当前发牌的人数为：" + players.size());
            System.err.println(home);
            for (int i = 0; i < players.size(); i++) {
                Player player2 = players.get(i);
                player2.setStatus("3");
                Channel channel1 = player2.getChannel();
                PokerS2C_DTO pokerS2C_dto = new PokerS2C_DTO();
                pokerS2C_dto.addPoker(player2.getPoker(), chairId, chairId);
                socketPackage.setProtocol(new Protocol(2, 11));
                socketPackage.setDatagram(pokerS2C_dto);
                //TODO 判断是否名牌,如果名牌,就通知给所有玩家
                if (home.getHomeInfo().getMethod(3)) {
                    playerInfoNotify.operator(home, socketPackage);
                } else {
                    logger.info("当前发牌的人：" + player2.getUid());
                    channel1.writeAndFlush(socketPackage);
                }
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //
            Player dizhu = home.getPlayerByChairId(chairId);

            home.listeningSchedule(dizhu, new Runnable() {
                @Override
                public void run() {
                    int operatorStatus = dizhu.getOperatorStatus();
                    if (operatorStatus != 1 || operatorStatus != 2) {
                        //标识为操作,通知所有人
                        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder().currentChairId(home.getNextOperaChairId(Integer.parseInt(dizhu.getChairId())) + "")
                                .preCharid(-1 + "").pokers(new ArrayList<>(1))
                                .operationStatus("-1")
                                .build();
                        dizhu.setOperatorStatus(2);
                        socketPackage.setProtocol(new Protocol(2, 12));
                        socketPackage.setDatagram(operatorS2CDto);
                        playerInfoNotify.operator(home, socketPackage);
                    }
                }
            }, 15, new FutureCallback() {
                @Override
                public void onSuccess(Object result) {
                    System.out.println("15未操作");
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });


        }
        return socketPackage;
    }

    @SubOperation(sub = 14)
    private SocketPackage isAllow(SocketPackage socketPackage, Channel channel) {
        CheckPokerC2S_DTO datagram = (CheckPokerC2S_DTO) socketPackage.getDatagram();
        String hid = datagram.getHid();
        String charid = datagram.getCharid();
        Home home = GameHelper.homeManager().getHome(hid);
        //获取出牌
        List<Card> pokerAsList = datagram.getPokerAsList();
        //获取桌面上牌信息
        List<Card> currentCards = home.getCurrentCards();
        System.err.println("当前牌桌上的牌: " + currentCards);
        CardType preCardType = GameRule.getCardType(currentCards);
        System.err.println("当前牌桌上的牌类型: " + preCardType);
        System.err.println("当前牌: " + pokerAsList);
        //获取玩家出牌类型
        CardType myCardType = GameRule.getCardType(pokerAsList);
        if (myCardType == null) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前出牌不合法");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(errorDatagram);
        }
        System.err.println("当前牌类型: " + myCardType);
        boolean overcomePrev = GameRule.isOvercomePrev(pokerAsList, myCardType, currentCards, preCardType);
        boolean cardTypeIsAllow = GameRule.getCardType(pokerAsList) == null ? false : true;
        //判断上一个出牌的人,是否是当前人,如果是cardTypeIsAllow设置为true
        if (home.getCurrentOutCardsPlayer() == null) {
            cardTypeIsAllow = true;
            overcomePrev = true;
        } else {
            if (home.getCurrentOutCardsPlayer().getChairId().equalsIgnoreCase(charid)) {
                cardTypeIsAllow = true;
                overcomePrev = true;
            }
        }
        System.err.println("是否可以出牌: " + overcomePrev);
        if (currentCards == null) {
            if (cardTypeIsAllow) {
                socketPackage.setDatagram(new CheckPokerS2C_DTO(true));
            } else {
                socketPackage.setDatagram(new CheckPokerS2C_DTO(false));
            }
            channel.writeAndFlush(socketPackage);
        } else {
            if (cardTypeIsAllow) {
                socketPackage.setDatagram(new CheckPokerS2C_DTO(overcomePrev));
            } else {
                socketPackage.setDatagram(new CheckPokerS2C_DTO(false));
            }
            channel.writeAndFlush(socketPackage);
        }
        return socketPackage;
    }

    /**
     * 操作是9
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    private SocketPackage cards(SocketPackage socketPackage, Channel channel) {
        OperatorC2S_DTO operatorC2SDto = (OperatorC2S_DTO) socketPackage.getDatagram();
        String hid = operatorC2SDto.getHid();
        String chairId = operatorC2SDto.getChairId();
        String operationStatus = operatorC2SDto.getOperationStatus();
        Home home = GameHelper.homeManager().getHome(hid);
        //设置玩家状态为9
        home.getPlayerByChairId(Integer.parseInt(chairId)).setOperatorStatus(Integer.parseInt(operationStatus));
        //添加操作次数
        home.addOperaCount();
        List<Card> currentCards = home.getCurrentCards();
        System.out.println("当前牌桌上的牌: " + currentCards);
        CardType preCardType = GameRule.getCardType(currentCards);
        System.out.println("当前牌桌上的牌类型: " + preCardType);
        //广播给所有人
        List<Card> pokerAsList = operatorC2SDto.getPokerAsList();
        System.out.println("当前牌: " + pokerAsList);
        CardType myCardType = GameRule.getCardType(pokerAsList);
        if (myCardType == null) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前出牌不合法");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(errorDatagram);
        }
        if (myCardType.equals(CardType.ZHA_DAN)) {
            home.multiply(2);
        }
        System.out.println("当前牌类型: " + myCardType);
        boolean cardTypeIsAllow = GameRule.isOvercomePrev(pokerAsList, myCardType, currentCards, preCardType);


        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .currentStatus("9")
                .preCharid(chairId).build();

        //判断上一个出牌的人,是否是当前人,如果是cardTypeIsAllow设置为true
        if (home.getCurrentOutCardsPlayer() == null) {
            cardTypeIsAllow = true;
        } else {
            if (home.getCurrentOutCardsPlayer().getChairId().equalsIgnoreCase(chairId)) {
                cardTypeIsAllow = true;
            }
        }
        //如果当前牌面上没牌
        if (currentCards == null) {
            //如果当前牌面没有牌,且出牌符合规则就允许出牌
            if (myCardType != null) {
                cardTypeIsAllow = true;
            }
        }
        System.out.println("是否可以出牌: " + cardTypeIsAllow);
        Player myPlayer = home.getPlayerByChairId(Integer.parseInt(chairId));
        if (cardTypeIsAllow) {
            //当前出牌的人
            //当玩家出牌后,就移除出下去的牌
            myPlayer.removePoker(pokerAsList);
            //设置当前牌面上牌
            home.setCurrentCards(pokerAsList);
            //设置当前牌面上牌的玩家
            home.setCurrentOutCardsPlayer(myPlayer);
            List<String> pokerAsString = new ArrayList<>();
            for (Card card : pokerAsList) {
                pokerAsString.add(String.valueOf(card.id));
            }
            operatorS2CDto.setPokers(pokerAsString);
            socketPackage.setDatagram(operatorS2CDto);
            playerInfoNotify.operator(home, socketPackage);
        } else {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前出牌不合法");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(errorDatagram);
        }
        System.out.println("当前玩家uid:" + myPlayer.getUid() + ",剩余牌数:" + myPlayer.getPoker());
        //记录玩家将要操作的状态
        Player willOptPlayer = home.getPlayerByChairId(Integer.parseInt(operatorS2CDto.getCurrentChairId()));
        willOptPlayer.setWillOperatorStatus(Integer.parseInt(operatorS2CDto.getCurrentStatus()));
        if (myPlayer.getPoker().size() == 0) {
            operatorS2CDto.setCurrentChairId("-1");
            //TODO 结算
            SettleS2C_DTO settleS2C_dto = new SettleS2C_DTO();
            settleS2C_dto.setBlind(home.getHomeInfo().getBlind());
            settleS2C_dto.setCharid(myPlayer.getChairId());
            HomeInfo homeInfo = home.getHomeInfo();
            String homeOwnerUid = homeInfo.getHomeOwner().getUid();
            String cardNum = mongoDao.findByUid(homeOwnerUid, UserEntity.class).getCardNum();
            settleS2C_dto.setCardNum(cardNum);
            homeInfo.subShengyuJvshu();
            settleS2C_dto.setRoomNum(homeInfo.getRoomNum());
            settleS2C_dto.setCurrentRoomNum(homeInfo.getShengyuRoomNum() + "");
            List<SettleS2C_DTO.SettleDTO> settleDTOS = new ArrayList<>();
            //当前倍数
            int multiple = home.getMultiple();
            //当前人数
            //获取地主座位
            int landLordChairId = home.getLandLordChairId();
            String uid = myPlayer.getUid();
            String blind = homeInfo.getBlind();
            String grade = (home.getPlayers().size() - 1) * multiple * Integer.parseInt(blind) + "";
            if (Integer.parseInt(chairId) == landLordChairId) {
                //地主赢
                int dizhuCountGrade = myPlayer.addGrade(Integer.parseInt(grade));
                settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid, dizhuCountGrade+""));
                myPlayer.setCurrentGrage(grade);
                Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                    @Override
                    public boolean test(Player player) {
                        return !player.getUid().equalsIgnoreCase(uid);
                    }
                });
                ArrayList<Player> players = Lists.newArrayList(playerStream.iterator());
                for (int i = 0, lenth = players.size(); i < lenth; i++) {
                    Player player = players.get(i);
                    String uid0 = player.getUid();
                    int nongMinCountGrade = player.subGrade(multiple * Integer.parseInt(blind));
                    settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid0, "" + nongMinCountGrade + "", CardUtil.cardConvert(player.getPoker())));
                    player.setCurrentGrade("-" + multiple * Integer.parseInt(blind) + "");
                }
            } else {
                Player playerByChairId1 = home.getPlayerByChairId(landLordChairId);
                String uid1 = playerByChairId1.getUid();
                Player playerByChairId = home.getPlayerByChairId(landLordChairId);
                //农民赢
                Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                    @Override
                    public boolean test(Player player) {
                        return !player.getUid().equalsIgnoreCase(uid1);
                    }
                });
                ArrayList<Player> players = Lists.newArrayList(playerStream.iterator());
                for (int i = 0, lenth = players.size(); i < lenth; i++) {
                    Player player = players.get(i);
                    String uid0 = player.getUid();
                    int nongMinCountGrade = player.subGrade(multiple * Integer.parseInt(blind));
                    settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid0, nongMinCountGrade+ "", CardUtil.cardConvert(player.getPoker())));
                    player.setCurrentGrade(multiple * Integer.parseInt(blind) + "");
                }
                int diZhuCountGrade = playerByChairId.subGrade(Integer.parseInt(grade));
                settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid1 + "", "" + diZhuCountGrade, CardUtil.cardConvert(playerByChairId.getPoker())));
                playerByChairId1.setCurrentGrade("-" + grade);
            }
            settleS2C_dto.setUserSettles(settleDTOS);
            //将结算信息发给其他人
            socketPackage.setProtocol(new Protocol(2, 15));
            socketPackage.setDatagram(settleS2C_dto);
            playerInfoNotify.operator(home, socketPackage);

            //清理房间地主
            home.setFirstJiaoDizhuCharid(-1);
            home.setLandLordChairId(-1);
            home.setCurrentOutCardsPlayer(null);
            home.setNextOutCard(false);
            home.setMaxCardOut(false);
            home.setCurrentCards(null);
            home.setReadyNum(0);
            home.getPlayers().stream().forEach(player -> player.getPoker().clear());
            home.getPoker().getMainPoker().clear();
            home.setPoker(new CardPoker(home.getPlayers().size()));
            long startTime = home.getStartTime();
            List<Player> players = home.getPlayers();
            for (int i = 0, len = players.size(); i < len; i++) {
                Player player = players.get(i);
                mongoDao.insert(new UserFighting(player.getUid(), hid, startTime, System.currentTimeMillis(), player.getCurrentGrade()));
            }
        }
        return socketPackage;
    }

    /**
     * 当前操作状态
     * 叫地主 = 1,
     * 不叫 = 2,
     * 抢地主 = 3,
     * 不抢 = 4,
     * 加倍 = 5,
     * 不加倍 = 6,
     * 思考中 = 7,
     * 不出 = 8,
     * 出牌 = 9,
     * 其他 = 10
     */
    public SocketPackage Qiang(SocketPackage socketPackage, Channel channel) {
        OperatorC2S_DTO operatorC2SDto = (OperatorC2S_DTO) socketPackage.getDatagram();
        String operationStatus = operatorC2SDto.getOperationStatus();
        String chairId = operatorC2SDto.getChairId();
        String hid = operatorC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player curretnPlayer = home.getPlayerByChairId(Integer.parseInt(chairId));
        int old_operaStatus = curretnPlayer.getOperatorStatus();
        curretnPlayer.setOperatorStatus(Integer.parseInt(operationStatus));
        //取消用户身上的监控器
        ListenableScheduledFuture schedule = home.getSchedule(curretnPlayer);
        if (schedule != null) {
            schedule.cancel(true);
        }
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .preCharid(chairId).pokers(new ArrayList<>()).build();
        System.err.println("---------------------:抢地主入口打印:--------------------- ");
        System.err.println(home);
        //当前人叫地主
        if (operationStatus.equalsIgnoreCase("1")) {
            //添加抢地主次数
            home.addQiangDiZhuCount();
            //通知下一个人抢地主
            operatorS2CDto.setCurrentStatus("3");
            curretnPlayer.setJiaodizhu(true);
            //设置第一个叫地主的座位
            home.setFirstJiaoDizhuCharid(Integer.parseInt(chairId));
            socketPackage.setDatagram(operatorS2CDto);

            //判断其他人都是2221: 最后修改的地方
            Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    return !player.getChairId().equalsIgnoreCase(home.getFirstQiangDizhuCharid() + "");
                }
            });
            boolean isOutCard = playerStream.allMatch(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    return player.getOperatorStatus() == 2;
                }
            });

            if (isOutCard) {
                home.setLandLordChairId(home.getFirstQiangDizhuCharid());
                socketPackage.setProtocol(new Protocol(2, 13));
                ArrayList<Card> mainPoker = home.getPoker().getMainPoker();
                List<String> mainPokers = new ArrayList<>();
                mainPoker.stream().forEach(card -> {
                    mainPokers.add(card.id + "");
                });
                chairId = home.getFirstQiangDizhuCharid() + "";
                DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(chairId).pokers(mainPokers).build();
                home.setCurrentOutCardsPlayer(curretnPlayer);//这是
                socketPackage.setDatagram(dizhuS2C_dto);
                playerInfoNotify.operator(home, socketPackage);
                //将底牌，添加到地主牌中
                int landLordChairId = home.getLandLordChairId();
                home.getPlayerByChairId(landLordChairId).addMainPoker(mainPoker);

                //通知地主出牌
                operatorS2CDto.setCurrentStatus("9");
                operatorS2CDto.setCurrentChairId(chairId);
                operatorS2CDto.setOperationStatus("-1");
                socketPackage.setProtocol(new Protocol(2, 12));
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
                return socketPackage;
            }
            //记录玩家将要操作的状态
            Player willOptPlayer = home.getPlayerByChairId(Integer.parseInt(operatorS2CDto.getCurrentChairId()));
            willOptPlayer.setWillOperatorStatus(Integer.parseInt(operatorS2CDto.getCurrentStatus()));
            playerInfoNotify.operator(home, socketPackage);
            System.err.println("------------------:抢地主后打印:----------------- ");
            System.err.println(home);
            return socketPackage;
        }
        //当前人不叫
        if (operationStatus.equalsIgnoreCase("2")) {
            //添加抢地主次数
            home.addQiangDiZhuCount();
            //通知下一个人叫地主
            operatorS2CDto.setCurrentStatus("1");
            //抢地主就吧当前地主改为强的人
            home.setLandLordChairId(Integer.parseInt(chairId));
            curretnPlayer.setJiaodizhu(false);
            //TODO 如果只有地主抢地主,则确定地主,并将底牌通知给他
            //判断是否没人抢地主
            Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    return !player.getChairId().equalsIgnoreCase(home.getFirstQiangDizhuCharid() + "");
                }
            });
            boolean isOutCard = playerStream.allMatch(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    return player.getOperatorStatus() == 4;
                }
            });
            if (isOutCard) {//通知地主出牌
                //地主已经确定为是第一个叫地主的人
                home.setLandLordChairId(home.getFirstQiangDizhuCharid());
                socketPackage.setProtocol(new Protocol(2, 13));
                ArrayList<Card> mainPoker = home.getPoker().getMainPoker();
                List<String> mainPokers = new ArrayList<>();
                mainPoker.stream().forEach(card -> {
                    mainPokers.add(card.id + "");
                });
                chairId = home.getFirstQiangDizhuCharid() + "";
                DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(chairId).pokers(mainPokers).build();
                home.setCurrentOutCardsPlayer(curretnPlayer);//这是
                socketPackage.setDatagram(dizhuS2C_dto);
                playerInfoNotify.operator(home, socketPackage);

                //将底牌，添加到地主牌中
                int landLordChairId = home.getLandLordChairId();
                home.getPlayerByChairId(landLordChairId).addMainPoker(mainPoker);
                //通知地主出牌
                operatorS2CDto.setCurrentStatus("9");
                operatorS2CDto.setCurrentChairId(chairId);
                operatorS2CDto.setOperationStatus("-1");
                socketPackage.setProtocol(new Protocol(2, 12));
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
            } else {
                operatorS2CDto.setCurrentStatus("1");
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
            }
            return socketPackage;

        }
        //抢地主
        if (operationStatus.equalsIgnoreCase("3")) {
            home.multiply(2);
            //添加抢地主次数
            home.addQiangDiZhuCount();
            //通知下一个人抢地主
            operatorS2CDto.setCurrentStatus("3");
            home.setLandLordChairId(Integer.parseInt(chairId));
            //通知下一个玩家抢地主,如果他的状态是不抢就跳过
            if (home.getQiangDiZhuCount() % 4 == 0) {
                chairId = home.getFirstQiangDizhuCharid() + "";
                operatorS2CDto.setCurrentChairId(chairId);
                //通知下一个人抢地主
                operatorS2CDto.setCurrentStatus("3");
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
                return socketPackage;
            }
            //是否抢地主
            if (home.isDizhu()) {
                socketPackage.setProtocol(new Protocol(2, 13));
                ArrayList<Card> mainPoker = home.getPoker().getMainPoker();
                List<String> mainPokers = new ArrayList<>();
                mainPoker.stream().forEach(card -> {
                    mainPokers.add(card.id + "");
                });
                DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(chairId).pokers(mainPokers).build();
                home.setCurrentOutCardsPlayer(curretnPlayer);//这是
                home.setLandLordChairId(Integer.parseInt(chairId));
                socketPackage.setDatagram(dizhuS2C_dto);
                playerInfoNotify.operator(home, socketPackage);

                //将底牌，添加到地主牌中
                int landLordChairId = home.getLandLordChairId();
                home.getPlayerByChairId(landLordChairId).addMainPoker(mainPoker);
                //通知地主出牌
                operatorS2CDto.setCurrentStatus("9");
                operatorS2CDto.setCurrentChairId(chairId);
                operatorS2CDto.setOperationStatus("-1");
                socketPackage.setProtocol(new Protocol(2, 12));
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
            } else {
                operatorS2CDto.setCurrentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "");
                //通知下一个人抢地主
                operatorS2CDto.setCurrentStatus("3");
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
            }

            return socketPackage;
        }
        //不抢地主
        if (operationStatus.equalsIgnoreCase("4")) {
            //添加抢地主次数
            home.addQiangDiZhuCount();
            //TODO 判断出了地主,其他人是不是都是4
            //判断出了地主其他人,是否都是不出,获取所有玩家,并排除当前最大玩家
            Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    boolean b = player.getChairId().equalsIgnoreCase(home.getFirstQiangDizhuCharid() + "");
                    return !b;
                }
            });
            //? 当true: 说明,没人抢地主,可以,直接发牌
            //判断除地主其他人是否是不出或者出牌
            boolean isMaxOut0 = playerStream.allMatch(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    return player.getOperatorStatus() == 4;
                }
            });
            /**
             * 当其他人都不叫,只有地主叫,确定地主为第一个叫地主的人
             */
            if (isMaxOut0) {
                home.setLandLordChairId(home.getFirstQiangDizhuCharid());
                socketPackage.setProtocol(new Protocol(2, 13));
                ArrayList<Card> mainPoker = home.getPoker().getMainPoker();
                List<String> mainPokers = new ArrayList<>();
                mainPoker.stream().forEach(card -> {
                    mainPokers.add(card.id + "");
                });
                DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(home.getLandLordChairId() + "").pokers(mainPokers).build();
                socketPackage.setDatagram(dizhuS2C_dto);
                playerInfoNotify.operator(home, socketPackage);

                //将底牌，添加到地主牌中
                int landLordChairId = home.getLandLordChairId();
                home.getPlayerByChairId(landLordChairId).addMainPoker(mainPoker);
                //通知地主出牌
                operatorS2CDto.setCurrentStatus("9");
                operatorS2CDto.setCurrentChairId(home.getLandLordChairId() + "");
                operatorS2CDto.setOperationStatus("-1");
                socketPackage.setProtocol(new Protocol(2, 12));
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
            } else if (home.getFirstQiangDizhuCharid() == Integer.parseInt(chairId)) {
                //如果是地主,第一次叫地主,第二次不叫,则给最后一个抢地主的人
                socketPackage.setProtocol(new Protocol(2, 13));
                ArrayList<Card> mainPoker = home.getPoker().getMainPoker();
                List<String> mainPokers = new ArrayList<>();
                mainPoker.stream().forEach(card -> {
                    mainPokers.add(card.id + "");
                });
                DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(home.getLandLordChairId() + "").pokers(mainPokers).build();
                socketPackage.setDatagram(dizhuS2C_dto);
                playerInfoNotify.operator(home, socketPackage);

                //通知地主出牌
                operatorS2CDto.setCurrentStatus("9");
                operatorS2CDto.setCurrentChairId(home.getLandLordChairId() + "");
                operatorS2CDto.setOperationStatus("-1");
                socketPackage.setProtocol(new Protocol(2, 12));
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);

                //将底牌，添加到地主牌中
                int landLordChairId = home.getLandLordChairId();
                home.getPlayerByChairId(landLordChairId).addMainPoker(mainPoker);

            } else {
                operatorS2CDto.setCurrentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "");
                //通知下一个人抢地主
                operatorS2CDto.setCurrentStatus("3");
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
            }
            return socketPackage;
        }
        return socketPackage;

    }


    public SocketPackage JiaBei(SocketPackage socketPackage, Channel channel) {
        OperatorC2S_DTO operatorC2SDto = (OperatorC2S_DTO) socketPackage.getDatagram();
        String operationStatus = operatorC2SDto.getOperationStatus();
        String chairId = operatorC2SDto.getChairId();
        String hid = operatorC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player playerByChairId = home.getPlayerByChairId(Integer.parseInt(chairId));
        int old_operaStatus = playerByChairId.getOperatorStatus();
        playerByChairId.setOperatorStatus(Integer.parseInt(operationStatus));
        //取消用户身上的监控器
        ListenableScheduledFuture schedule = home.getSchedule(playerByChairId);
        if (schedule != null) {
            schedule.cancel(true);
        }
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .preCharid(chairId).pokers(new ArrayList<>()).build();
        System.err.println("加倍打印: ");
        System.err.println(home);
        //当前人加倍
        if (operationStatus.equalsIgnoreCase("5")) {
            home.multiply(2);
            //通知下一个人加倍
            operatorS2CDto.setCurrentStatus("5");
        }

        //当前人不加倍
        if (operationStatus.equalsIgnoreCase("6")) {
            //通知下一个人加倍
            operatorS2CDto.setCurrentStatus("5");
        }
        //记录玩家将要操作的状态
        Player willOptPlayer = home.getPlayerByChairId(Integer.parseInt(operatorS2CDto.getCurrentChairId()));
        willOptPlayer.setWillOperatorStatus(Integer.parseInt(operatorS2CDto.getCurrentStatus()));
        return socketPackage;
    }


    public SocketPackage opera(SocketPackage socketPackage, Channel channel) {
        OperatorC2S_DTO operatorC2SDto = (OperatorC2S_DTO) socketPackage.getDatagram();
        String operationStatus = operatorC2SDto.getOperationStatus();
        String chairId = operatorC2SDto.getChairId();
        String hid = operatorC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player currentPlayer = home.getPlayerByChairId(Integer.parseInt(chairId));
        currentPlayer.setOperatorStatus(Integer.parseInt(operationStatus));
        //取消用户身上的监控器
        ListenableScheduledFuture schedule = home.getSchedule(currentPlayer);
        if (schedule != null) {
            schedule.cancel(true);
        }
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .preCharid(chairId).pokers(new ArrayList<>()).build();
//        System.err.println("全局打印: ");
//        System.err.println(home);

        int i = Integer.parseInt(operatorC2SDto.getOperationStatus());
        if (i <= 4) {
            //抢地主逻辑
            SocketPackage qiang = Qiang(socketPackage, channel);
            return qiang;
        } else if (i > 4 & i < 7) {
            //加倍逻辑
            return JiaBei(socketPackage, channel);
        }
        //其他为出牌逻辑
        //当前人出牌
        if (operationStatus.equalsIgnoreCase("9")) {
            //通知下一个人出牌
            operatorS2CDto.setCurrentStatus("9");
            //添加操作次数
            home.addOperaCount();
            home.setNextOutCard(true);
        }
        //当前人不出
        if (operationStatus.equalsIgnoreCase("8")) {
            System.err.println("出牌打印: ");
            System.err.println(home);
            //通知下一个人出牌
            operatorS2CDto.setCurrentStatus("9");
            //添加操作次数
            home.addOperaCount();
            boolean isMaxOut = false;
            //TODO 判断是下一个人出,还是最大人出
            //true: 新的一轮开始
            if (home.isAgain()) {
                if (currentPlayer.getOperatorStatus() == 8) {
                    isMaxOut = true;
                }
            }
            //判断出了地主其他人,是否都是不出,获取所有玩家,并排除当前最大玩家
            Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    boolean b = player.getChairId().equalsIgnoreCase(home.getCurrentOutCardsPlayer().getChairId());
                    return !b;
                }
            });
            //? 当true: 最大人出牌
            //判断除地主其他人是否是不出或者出牌
            boolean isMaxOut0 = playerStream.allMatch(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    //如果当前桌面牌状态是出牌,其他人是不出,则是最大人出牌
                    if (home.getCurrentOutCardsPlayer().getUid().equalsIgnoreCase(player.getUid())) {
                        if (player.getOperatorStatus() == 9) {
                            return true;
                        }
                    }
                    return player.getOperatorStatus() == 8;
                }
            });
            //当等于true,代表新的一轮开始
            if (isMaxOut & isMaxOut0) {
                //是否最大牌,继续出牌
                home.setMaxCardOut(true);
                //清除当前牌面上牌,牌
                home.setCurrentCards(null);
                //设置最大人出牌
                operatorS2CDto.setCurrentChairId(home.getCurrentOutCardsPlayer().getChairId() + "");
                home.getPlayers().stream().filter(new Predicate<Player>() {
                    @Override
                    public boolean test(Player player) {
                        return !player.getChairId().equalsIgnoreCase(home.getCurrentOutCardsPlayer().getChairId());
                    }
                }).forEach(p -> p.setOperatorStatus(10));
            } else if (!isMaxOut) {
                home.setNextOutCard(true);
            }
        }
        socketPackage.setProtocol(new Protocol(2, 12));
        socketPackage.setDatagram(operatorS2CDto);
        //当,除了地主其他人都已经完成加倍或者不加倍操作,说明开始出牌
        if (home.isOutCard()) {
            home.setStartTime(System.currentTimeMillis());
            //是否最大人出牌
            if (home.isMaxCardOut()) {
                home.setMaxCardOut(false);
                //设置出最大人,其他人的操作都为10
                home.getPlayers().stream().filter(new Predicate<Player>() {
                    @Override
                    public boolean test(Player player) {
                        return !player.getChairId().equalsIgnoreCase(home.getCurrentOutCardsPlayer().getChairId());
                    }
                }).forEach(p -> p.setOperatorStatus(10));
            } else if (home.isNextOutCard()) {
                home.setNextOutCard(false);
                //设置下一人出牌
                operatorS2CDto.setCurrentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "");
            } else {
                //设置地主出牌
                operatorS2CDto.setCurrentChairId(home.getLandLordChairId() + "");
            }
            //设置下一个人出牌
            operatorS2CDto.setCurrentStatus("9");
            //上一个出牌玩家就是当前请求玩家
            operatorS2CDto.setPreCharid(chairId);
            socketPackage.setDatagram(operatorS2CDto);
            //记录玩家将要操作的状态
            Player willOptPlayer = home.getPlayerByChairId(Integer.parseInt(operatorS2CDto.getCurrentChairId()));
            willOptPlayer.setWillOperatorStatus(Integer.parseInt(operatorS2CDto.getCurrentStatus()));
            playerInfoNotify.operator(home, socketPackage);
        } else {
            //还是操作,广播给其他玩家
            playerInfoNotify.operator(home, socketPackage);
        }
        return socketPackage;
    }




}
