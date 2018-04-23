package smile.service.handler;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.json.JsonUtils;
import smile.config.ErrorEnum;
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
import smile.tool.DdzOperaHandler;
import smile.tool.GameHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @Package: smile.service.handler
 * @Description: 1. cards 出牌操作
 * 2. 叫地主抢地主操作
 * 3. 加倍操作
 * 4. 校验出牌信息
 * @date: 2018/4/16 下午11:58
 * @author: liuxin
 */
@SmileComponent
@Action
public class PlayerOperaActionHandler extends AbstractActionHandler {
    private static Logger logger = LoggerFactory.getLogger(PlayerOperaActionHandler.class);

    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;

    @InsertBean
    DdzOperaHandler ddzOperaHandler;

    /**
     * 根据是否出牌判断逻辑处理
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 12,model = OperatorC2S_DTO.class)
    public SocketPackage operator(SocketPackage socketPackage, Channel channel) {
        OperatorC2S_DTO datagram = (OperatorC2S_DTO) socketPackage.getDatagram();
        String operationStatus = datagram.getOperationStatus();
        String hid = datagram.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
//        ddzOperaHandler.Operate(Integer.parseInt(operationStatus),home);
        if (datagram.getPokerAsList() != null) {
            cards(socketPackage, channel);
        } else {
            buchupai(socketPackage, channel);
        }
        return socketPackage;
    }

    /**
     * 1. 当玩家发送准备操作，将玩家的状态置换为2准备
     * 2. 在房间信息添加准备人数的标识，默认开始是0
     * 3. 构建玩家状态将玩家作为号和玩家当前状态返回
     * 4. 将当前玩家的状态推送给房间里其他的玩家
     * 5. 判断房间是否都是准备状态，如果readyNum等于4，则开始发牌操作
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 10,model = PlayerReadyC2S_DTO.class)
    public SocketPackage ready(SocketPackage socketPackage, Channel channel) {
        PlayerReadyC2S_DTO playerReadyC2SDto = (PlayerReadyC2S_DTO) socketPackage.getDatagram();
        String uid = playerReadyC2SDto.getUid();
        String hid = playerReadyC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player player = home.getPlayer(uid);
        //1.设置玩家为准备状态
        player.setStatus("2");
        //2.
        home.addReady();
        //3.
        PlayerStatusS2C_DTO playStatus = PlayerStatusS2C_DTO.builder()
                .chairId(player.getChairId()).status(player.getStatus())
                .uid(uid).build();
        socketPackage.setProtocol(new Protocol(2, 10));
        socketPackage.setDatagram(playStatus);
        //4.
        List<Player> players = home.getPlayers();
        for (int j = 0; j < players.size(); j++) {
            Player player2 = players.get(j);
            Channel otherChannel = player2.getChannel();
            otherChannel.writeAndFlush(socketPackage);
        }
        //5. 当前玩家准备好,判断其他玩家是否也都已经准备,当已经准备时候发牌
        if (home.isStart()) {
            Poker poker = home.getPoker();
            poker.pokerShuffle();
            poker.deal(players);
            Random random = new Random();
            //地主座位号
            int chairId = random.nextInt(players.size());
            home.setInitLordChairId(chairId);
            //设置将要操作的玩家
            home.setCurrentChairId(chairId);
            /**
             * 通知地主操作
             */
            logger.info("当前发牌的人数为：" + players.size());
            logger.info(home.toString());
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
            /**
             * 1. 当开始发牌,将所有玩家的牌信息和地主座位号推送给自己，并将玩家状态由2准备，改为3游戏中
             * 2. 给地主添加一个超时动作，当15s后判断，如果当前地主，没有叫地主1，也没有不叫2，则通知
             *    下一个玩家去叫地主，并由服务器将地主超时操作该为2，不叫
             */
            //1.
            for (int i = 0; i < players.size(); i++) {
                Player ownerPlayer = players.get(i);
                ownerPlayer.setStatus("3");
                Channel channel1 = ownerPlayer.getChannel();
                PokerS2C_DTO pokerS2C_dto = new PokerS2C_DTO();
                pokerS2C_dto.addPoker(ownerPlayer.getPoker(), chairId, chairId);
                socketPackage.setProtocol(new Protocol(2, 11));
                socketPackage.setDatagram(pokerS2C_dto);
                ChannelFuture channelFuture = channel1.writeAndFlush(socketPackage);
                try {
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            if (channelFuture.isSuccess()){
                                logger.info("当前发牌的人：" + ownerPlayer.getUid());
                                System.err.println("当前发送玩家连接信息："+ownerPlayer.getChannel());
                                System.err.println("发送poker:"+ownerPlayer.getPoker());
                            }
                        }
                    }).sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                try {
//                    java.lang.Thread.sleep(800L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            //2.
            Player dizhu = home.getPlayerByChairId(chairId);
            dizhu.setTimeOut(true, home);
        }
        return socketPackage;
    }

    /**
     * 验证是否可以出牌
     * 1. 获取将要验证的出牌
     * 2. 获取桌面上牌信息
     * 3. 获取将要验证牌的类型
     * 4. 判断是否可以出牌
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 14,model = CheckPokerC2S_DTO.class)
    private SocketPackage isAllow(SocketPackage socketPackage, Channel channel) {
        CheckPokerC2S_DTO datagram = (CheckPokerC2S_DTO) socketPackage.getDatagram();
        String hid = datagram.getHid();
        String charid = datagram.getCharid();
        Home home = GameHelper.homeManager().getHome(hid);
        //1.
        List<Card> pokerAsList = datagram.getPokerAsList();
        //2.
        List<Card> currentCards = home.getCurrentCards();
        logger.info("当前牌桌上的牌: " + currentCards);
        CardType preCardType = GameRule.getCardType(currentCards);
        logger.info("当前牌桌上的牌类型: " + preCardType);
        logger.info("当前将要验证的牌: " + pokerAsList);
        //3.
        CardType myCardType = GameRule.getCardType(pokerAsList);
        if (myCardType == null) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.CHUPAI_FEIFA);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(errorDatagram);
        }
        logger.info("当前将要验证的牌类型: " + myCardType);
        //4.
        boolean overcomePrev = GameRule.isOvercomePrev(pokerAsList, myCardType, currentCards, preCardType);
        boolean cardTypeIsAllow = GameRule.getCardType(pokerAsList) == null ? false : true;
        if (home.getCurrentOutCardsPlayer() == null) {
            //当没有人出牌时候，当前出牌玩家为null，可以出牌
            cardTypeIsAllow = true;
            overcomePrev = true;
        } else {
            //判断上一个出牌的人,是否是当前人,如果是cardTypeIsAllow设置为true
            if (home.getCurrentOutCardsPlayer().getChairId().equalsIgnoreCase(charid)) {
                cardTypeIsAllow = true;
                overcomePrev = true;
            }
        }
        logger.info("是否可以出牌: " + overcomePrev);
        //当前还没有出牌
        if (currentCards == null) {
            //只要规则符合出牌规则就可以出牌
            if (cardTypeIsAllow) {
                socketPackage.setDatagram(new CheckPokerS2C_DTO(true));
            } else {
                socketPackage.setDatagram(new CheckPokerS2C_DTO(false));
            }
            channel.writeAndFlush(socketPackage);
        } else {
            //当前已经有人出牌，则根据验证结果出牌
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
        logger.info("当前牌桌上的牌: " + currentCards);
        CardType preCardType = GameRule.getCardType(currentCards);
        logger.info("当前牌桌上的牌类型: " + preCardType);
        //广播给所有人
        List<Card> pokerAsList = operatorC2SDto.getPokerAsList();
        logger.info("当前牌: " + pokerAsList);
        CardType myCardType = GameRule.getCardType(pokerAsList);
        if (myCardType == null) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.CHUPAI_FEIFA);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(errorDatagram);
        }
        if (myCardType.equals(CardType.ZHA_DAN)) {
            home.multiply(2);
        }
        logger.info("当前牌类型: " + myCardType);
        boolean cardTypeIsAllow = GameRule.isOvercomePrev(pokerAsList, myCardType, currentCards, preCardType);

        String maxChairId = "-1";
        if (home.getCurrentOutCardsPlayer() != null) {
            maxChairId = home.getCurrentOutCardsPlayer().getChairId();
        }
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .maxOperaCharId(maxChairId)
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
        logger.info("是否可以出牌: " + cardTypeIsAllow);
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
            operatorS2CDto.setMaxOperaCharId(myPlayer.getChairId());
            socketPackage.setDatagram(operatorS2CDto);
            playerInfoNotify.operator(home, socketPackage);
        } else {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.CHUPAI_FEIFA);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(errorDatagram);
        }
        logger.info("当前玩家uid:" + myPlayer.getUid() + ",剩余牌数:" + myPlayer.getPoker());
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
                settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid, dizhuCountGrade + ""));
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
                    settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid0, nongMinCountGrade + "", CardUtil.cardConvert(player.getPoker())));
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
            home.clearFirstJiaoDiZhu();
            home.setLandLordChairId(-1);
            home.setCurrentOutCardsPlayer(null);
            home.setNextOutCard(false);
            home.setMaxCardOut(false);
            home.setCurrentCards(null);
            home.setReadyNum(0);
            home.getPlayers().stream().forEach(player -> {player.getPoker().clear();player.setStatus("1");});
            home.getPoker().getMainPoker().clear();
            home.setPoker(new CardPoker(home.getPlayers().size()));
            home.setQiangDiZhuCount(0);
            home.setOperaCount(0);
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
        ddzOperaHandler.Operate(Integer.parseInt(operationStatus), home);
        return null;
    }


    /**
     * 出牌操作
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    public SocketPackage buchupai(SocketPackage socketPackage, Channel channel) {
        OperatorC2S_DTO operatorC2SDto = (OperatorC2S_DTO) socketPackage.getDatagram();
        String operationStatus = operatorC2SDto.getOperationStatus();
        String chairId = operatorC2SDto.getChairId();
        String hid = operatorC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player currentPlayer = home.getPlayerByChairId(Integer.parseInt(chairId));
        currentPlayer.setOperatorStatus(Integer.parseInt(operationStatus));
        String maxChairId = "-1";
        if (home.getCurrentOutCardsPlayer() != null) {
            maxChairId = home.getCurrentOutCardsPlayer().getChairId();
        }

        int i = Integer.parseInt(operatorC2SDto.getOperationStatus());
        if (i <= 4) {
            //抢地主逻辑
            SocketPackage qiang = Qiang(socketPackage, channel);
            return qiang;
        } else if (i > 4 & i < 7) {
            //加倍逻辑
            return JiaBei(socketPackage, channel);
        }
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .maxOperaCharId(maxChairId)
                .preCharid(chairId).pokers(new ArrayList<>()).build();
        //当前人不出
        if (operationStatus.equalsIgnoreCase("8")) {
            System.err.println("出牌打印: ");
            System.err.println(home);
            //更新当前玩家操作
            currentPlayer.setOperatorStatus(Integer.parseInt(operationStatus));
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
                    Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                        @Override
                        public boolean test(Player player) {
                            boolean b = player.getChairId().equalsIgnoreCase(home.getCurrentOutCardsPlayer().getChairId());
                            return !b;
                        }
                    });
                    playerStream.forEach(player -> {
                        if (player.getOperatorStatus()==8){
                            //代表是上一轮的88
                            player.setOperatorStatus(88);
                        }
                    });
                }
            }
            // 判断出了地主其他人,是否都是不出,获取所有玩家,并排除当前最大玩家
            Stream<Player> playerStream = home.getPlayers().stream().filter(new Predicate<Player>() {
                @Override
                public boolean test(Player player) {
                    boolean b = player.getChairId().equalsIgnoreCase(home.getCurrentOutCardsPlayer().getChairId());
//                    return !b;
                    return true;
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
                //清除当前牌面上牌，以为自己的牌，没人要，还是自己出，所以可以清楚桌面上的牌
                home.setCurrentCards(null);
                //设置最大人出牌，还是自己出牌
                operatorS2CDto.setCurrentChairId(home.getCurrentOutCardsPlayer().getChairId() + "");
                //因为一局结束，所以更新其他玩家操作
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
            Player willOutCardPlayer = home.getPlayerByChairId(Integer.parseInt(operatorS2CDto.getCurrentChairId()));
            willOutCardPlayer.setWillOperatorStatus(Integer.parseInt(operatorS2CDto.getCurrentStatus()));
            playerInfoNotify.operator(home, socketPackage);
            //TODO 给下一个将要操作的人，添加一个监听
            //还是操作,广播给其他玩家
            String currentChairId = operatorS2CDto.getCurrentChairId();
            //默认给将要出牌的玩家设置为超时，如果用户没有超时
        } else {
            //还是操作,广播给其他玩家
            String currentChairId = operatorS2CDto.getCurrentChairId();
            Player willOutCardPlayer = home.getPlayerByChairId(Integer.parseInt(currentChairId));
            playerInfoNotify.operator(home, socketPackage);
//            //默认给将要出牌的玩家设置为超时，如果用户没有超时
//            willOutCardPlayer.setTimeOut(true,home);
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
        String maxChairId = "-1";
        if (home.getCurrentOutCardsPlayer() != null) {
            maxChairId = home.getCurrentOutCardsPlayer().getChairId();
        }
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                .operationStatus(operationStatus)
                .currentChairId(home.getNextOperaChairId(Integer.parseInt(chairId)) + "")
                .preCharid(chairId).pokers(new ArrayList<>())
                .maxOperaCharId(maxChairId).build();
        logger.info("加倍打印: ");
        logger.info(home.toString());
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


}
