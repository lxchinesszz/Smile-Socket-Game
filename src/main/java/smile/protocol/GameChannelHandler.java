package smile.protocol;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.json.JsonUtils;
import org.smileframework.tool.string.StringTools;
import smile.config.Constants;
import smile.database.domain.UserEntity;
import smile.database.dto.*;
import smile.database.mongo.MongoDao;
import smile.protocol.impl.*;
import smile.service.handler.PlayerInfoNotify;
import smile.service.home.*;
import smile.service.poker.*;
import smile.tool.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ChannelHandler.Sharable
public class GameChannelHandler extends ChannelInboundHandlerAdapter {
    private static final String TOKEN = "qwertyuio";

    private MongoDao mongoDao = IOC.get().getBean(MongoDao.class);

    private PlayerInfoNotify playerInfoNotify = IOC.get().getBean(PlayerInfoNotify.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接成功");
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = address.getHostName();
        String sessionId = UUID.randomUUID().toString().toUpperCase();
        ChannelAttributeTools.attr(ctx.channel(), Constants.SESSION_KEY, sessionId);
        ChannelAttributeTools.attr(ctx.channel(), Constants.IP_KEY, ip);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String HOSTADDRESS = ChannelAttributeTools.attr(ctx.channel(), Constants.IP_KEY);
//        System.out.println(HOSTADDRESS);
//        String HOSTADDRESS ="127.0.0.1";
//        System.out.println("客户端ip:" + HOSTADDRESS);
        SocketPackage socketPackage = (SocketPackage) msg;
        int sub = socketPackage.getProtocol().getSub();
        if (sub == 1) {
            //登录
            login(socketPackage, HOSTADDRESS, ctx.channel());
        } else if (sub == 3) {
            createHome(socketPackage, ctx.channel());
        } else if (sub == 4) {
            joinRoom(socketPackage, ctx.channel());
        } else if (sub == 5) {
            leaveHome(socketPackage, ctx.channel());
        } else if (sub == 10) {
            ready(socketPackage, ctx.channel());
        } else if (sub == 14) {
            isAllow(socketPackage, ctx.channel());
        } else if (sub == 16) {
            remove(socketPackage, ctx.channel());
        } else if (sub == 17) {
            charTM(socketPackage, ctx.channel());
        } else if (sub == 0) {
            socketPackage.setDatagram(new HeartbeatDatagram());
            ctx.channel().writeAndFlush(socketPackage);
        } else if (sub == 12) {
            OperatorC2S_DTO datagram = (OperatorC2S_DTO) socketPackage.getDatagram();
            if (datagram.getPokerAsList() != null) {
                cards(socketPackage, ctx.channel());
            } else {
                opera(socketPackage, ctx.channel());
            }
        } else if (sub == 100) {
            ServiceTokenDatagram serviceTokenDatagram = (ServiceTokenDatagram) socketPackage.getDatagram();
            if (!TOKEN.equalsIgnoreCase(serviceTokenDatagram.getServiceToken())) {
                ChannelFuture close = ctx.channel().disconnect();
                close.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            System.err.println("已经拒绝连接.....");
                        }
                    }
                });
            } else {
                socketPackage.setDatagram(new VersionDatagram("1.0.0"));
                ctx.channel().writeAndFlush(socketPackage);
            }
        }
    }


    public SocketPackage charTM(SocketPackage socketPackage, Channel channel) {
        ChatC2S_DTO datagram = (ChatC2S_DTO) socketPackage.getDatagram();
        String hid = datagram.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        playerInfoNotify.operator(home,socketPackage);
        return socketPackage;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }


    /**
     * qingli fangjain
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    private SocketPackage remove(SocketPackage socketPackage, Channel channel) {
        RemoveRoomC2S_DTO datagram = (RemoveRoomC2S_DTO) socketPackage.getDatagram();
        String hid = datagram.getHid();
        String uid = datagram.getUid();
        Home home = GameHelper.homeManager().getHome(hid);
        if (home == null) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前房间已经解散或不存在");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        HomeInfo homeInfo = home.getHomeInfo();
        String roomNum = homeInfo.getRoomNum();
        int shengyuRoomNum = homeInfo.getShengyuRoomNum();
        String ownerId = homeInfo.getHomeOwner().getUid();
        if (!ownerId.equalsIgnoreCase(uid)) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前uid无权解散房间");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
        }
        if (shengyuRoomNum == Integer.parseInt(roomNum)) {
            //TODO 将房卡信息,加入到用户
            int i = BigDecimal.valueOf(Integer.parseInt(roomNum)).divide(BigDecimal.valueOf(5L)).intValue();
            UserEntity byUid = mongoDao.findByUid(uid, UserEntity.class);
            String cardNum = byUid.getCardNum();
            byUid.setCardNum((Integer.parseInt(cardNum) + i) + "");
            String query = String.format("{\"uid\":\"%s\"}", uid);
            mongoDao.del(query, "ddz_user");
            mongoDao.insert(byUid);
        }
        Home rm_home = GameHelper.homeManager().clearHome(hid);
        RemoveRoomS2C_DTO.RemoveRoomS2C_DTOBuilder builder = RemoveRoomS2C_DTO.builder();
        if (rm_home != null) {
            socketPackage.setDatagram(builder.code("0").build());
        } else {
            socketPackage.setDatagram(builder.code("1").build());
        }
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }

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
                settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid, grade));
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
                    settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid0, "-" + multiple * Integer.parseInt(blind) + "", CardUtil.cardConvert(player.getPoker())));
                }
            } else {
                String uid1 = home.getPlayerByChairId(landLordChairId).getUid();
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
                    settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid0, multiple * Integer.parseInt(blind) + "", CardUtil.cardConvert(player.getPoker())));
                }

                settleDTOS.add(new SettleS2C_DTO().new SettleDTO(uid1 + "", "-" + grade,CardUtil.cardConvert(playerByChairId.getPoker())));
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

                //通知地主出牌
                operatorS2CDto.setCurrentStatus("9");
                operatorS2CDto.setCurrentChairId(chairId);
                operatorS2CDto.setOperationStatus("-1");
                socketPackage.setProtocol(new Protocol(2, 12));
                socketPackage.setDatagram(operatorS2CDto);
                playerInfoNotify.operator(home, socketPackage);
                return socketPackage;
            }
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
            playerInfoNotify.operator(home, socketPackage);
        } else {
            //还是操作,广播给其他玩家
            playerInfoNotify.operator(home, socketPackage);
        }
        return socketPackage;
    }

    public SocketPackage ready(SocketPackage socketPackage, Channel channel) {
        PlayerReadyC2S_DTO playerReadyC2SDto = (PlayerReadyC2S_DTO) socketPackage.getDatagram();
        String uid = playerReadyC2SDto.getUid();
        String hid = playerReadyC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        Player player = home.getPlayer(uid);
        player.setStatus("2");
        home.updatePlayer(player);
        home.addReady();
        PlayerStatusS2C_DTO playStatus = PlayerStatusS2C_DTO.builder().chairId(player.getChairId()).status(player.getStatus())
                .uid(uid).build();
        socketPackage.setProtocol(new Protocol(2, 10));
        socketPackage.setDatagram(playStatus);
        List<Player> players = home.getPlayers();
        for (int j = 0; j < players.size(); j++) {
            Player player2 = players.get(j);
            Channel otherChannel = player2.getChannel();
            otherChannel.writeAndFlush(socketPackage);
        }
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
                    channel1.writeAndFlush(socketPackage);
                }
                try {
                    Thread.sleep(200);
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


    public SocketPackage login(SocketPackage socketPackage, String ip, Channel channel) {
        /**
         * 判断该用户id是否已经注册,当已经注册,则直接返回
         * 当未注册则,就直接保存
         */
        UserDatagram datagram = (UserDatagram) socketPackage.getDatagram();
        String new_iconurl = datagram.getIconurl();
        datagram.setIp(ip);
        socketPackage.setDatagram(datagram);
        String accessToken = datagram.getAccessToken();
        String query = String.format("{\"accessToken\":\"%s\"}", accessToken);
        UserDatagram userDatagram = mongoDao.findOne(query, UserDatagram.class);
        if (userDatagram == null) {
            int i = StdRandom.generate6BitInt();
            datagram.setUid(String.valueOf(i));
            datagram.setCardNum("5");
            mongoDao.insert(datagram);
            socketPackage.setDatagram(datagram);
        } else {
            mongoDao.del(query, "ddz_user");
            if (StringTools.isEmpty(userDatagram.getUid())) {
                int i = StdRandom.generate6BitInt();
                userDatagram.setUid(String.valueOf(i));
            }
            userDatagram.setIconurl(new_iconurl);
            mongoDao.insert(userDatagram);
            socketPackage.setDatagram(userDatagram);
        }
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }

    public SocketPackage joinRoom(SocketPackage socketPackage, Channel channel) {
        JoinRoomC2S_DTO joinRoomC2S_dto = (JoinRoomC2S_DTO) socketPackage.getDatagram();
        String hid = joinRoomC2S_dto.getHid();
        String uid = joinRoomC2S_dto.getUid();
        ChannelAttributeTools.attr(channel, "hid", hid);
        ChannelAttributeTools.attr(channel, "uid", uid);
//        MongoDao mongoDao = IOC.get().getBean(MongoDao.class);
        String query = String.format("{\"uid\":\"%s\"}", uid);
        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
        Player player = new Player(uid, userInfo.getName(), channel);
        Home home = GameHelper.homeManager().getHome(hid);
        if (home == null) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前房间号:" + hid + ",已失效,请重新创建加入");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        Player isExit = home.getPlayer(uid);
        if (isExit == null) {
            List<Player> players = home.getPlayers();
            int size = players.size();
            ArrayList<String> objects = Lists.newArrayList();
            for (int i = 0; i < size; i++) {
                objects.add(players.get(i).getChairId());
            }
            List<String> strings = new ArrayList();
            strings.add("0");
            strings.add("1");
            strings.add("2");
            strings.add("3");
            strings.removeAll(objects);
            player.setChairId(String.valueOf(strings.get(0)));
            home.addPlayers(player);
        } else {
            player = isExit;
            player.setChannel(channel);
        }
        if (home.getPlayers().size() > home.getHomeInfo().getPersonNum()) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前房间玩家数量:" + home.getPlayers().size() + ", 房间最大玩家数:" + home.getHomeInfo().getPersonNum());
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            return socketPackage;
        }
        GameHelper.homeManager().updateHome(home);
        HomeInfo homeInfo = home.getHomeInfo();
        String blind = homeInfo.getBlind();
        String roomNum = homeInfo.getRoomNum();
//        JoinRoomS2C_DTO createRoomS2CDto = JoinRoomS2C_DTO.builder().blind(blind).hid(hid + "")
//                .ownerId(uid)
//                .roomNum(roomNum).build();
        Protocol protocol = socketPackage.getProtocol();

        CreateRoomS2C_DTO xin = CreateRoomS2C_DTO.builder().blind(blind).hid(hid + "")
                .ownerId(homeInfo.getHomeOwner().getUid())
                .roomNum(roomNum).multiple(homeInfo.getMultiple())
                .AA(homeInfo.getAA()).method(homeInfo.getMethod()).sharedIP(homeInfo.getSharedIP()).currentRoomNum(roomNum).build();
//        socketPackage.setDatagram(createRoomS2CDto);
//        socketPackage.setProtocol(new Protocol(2, 3));
        socketPackage.setDatagram(xin);
//        channel.writeAndFlush(socketPackage);

//        socketPackage.setDatagram(createRoomS2CDto);
        socketPackage.setProtocol(protocol);
        ChannelFuture channelFuture = channel.writeAndFlush(socketPackage);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    List<Player> players = home.getPlayers();
                    Player player2 = home.getPlayer(uid);
                    for (int i = 0; i < players.size(); i++) {
                        Player player1 = players.get(i);
                        Channel otherChannel = player1.getChannel();
                        String uid = player2.getUid();
                        String query = String.format("{\"uid\":\"%s\"}", uid);
                        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
                        //返回用户信息
                        PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                .chairId(player2.getChairId()).ip(userInfo.getIp()).gender(userInfo.getGender()).name(userInfo.getName()).iconurl(userInfo.getIconurl()).status(player2.getStatus()).uid(uid).build();
                        SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
                        otherChannel.writeAndFlush(playerInfoSocket);
//                        Thread.sleep(100);
                    }

                    Channel joinPlayer = player2.getChannel();
                    //将所有人信息在通知给Player2
                    for (int i = 0; i < players.size(); i++) {
                        Player player1 = players.get(i);
                        if (player1.getUid().equalsIgnoreCase(player2.getUid())) {
                            continue;
                        }
                        String uid = player1.getUid();
                        String query = String.format("{\"uid\":\"%s\"}", uid);
                        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
                        //返回用户信息
                        PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                .chairId(player1.getChairId()).ip(userInfo.getIp()).gender(userInfo.getGender()).name(userInfo.getName()).iconurl(userInfo.getIconurl()).status(player1.getStatus()).uid(player1.getUid()).build();
                        SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
                        joinPlayer.writeAndFlush(playerInfoSocket);
                    }
                }
            }
        });


        return socketPackage;
    }

    public SocketPackage createHome(SocketPackage socketPackage, Channel channel) {
        CreateRoomC2S_DTO homeDTO = (CreateRoomC2S_DTO) socketPackage.getDatagram();
        String uid = homeDTO.getUid();
        String personNum = homeDTO.getPersonNum();
        String blind = homeDTO.getBlind();
        String multiple = homeDTO.getMultiple();
        String roomNum = homeDTO.getRoomNum();
        String method = homeDTO.getMethod();
        String AA = homeDTO.getAA();
        String sharedIP = homeDTO.getSharedIP();
//
        String query = String.format("{\"uid\":\"%s\"}", uid);
        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
        int carNum = Integer.parseInt(userInfo.getCardNum());
        Integer.parseInt(roomNum);
        MongoDao mongoDao = IOC.get().getBean(MongoDao.class);
        int i = BigDecimal.valueOf(Integer.parseInt(roomNum)).divide(BigDecimal.valueOf(5L)).intValue();
        if (i > carNum) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前房卡数为:" + carNum + ", 可创建牌局数为:" + (carNum * 5));
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        userInfo.setCardNum(String.valueOf(carNum - i));
        String del_query = String.format("{\"uid\":\"%s\"}", uid);
        boolean ddz_user = mongoDao.del(del_query, "ddz_user");
        if (ddz_user) {
            mongoDao.insert(userInfo);
        }
        Player ownPlayer = new Player(uid, userInfo.getName(), channel);

        //创建一副扑克牌
        Poker poker = new CardPoker(personNum);
        //创建一个房管,对房间进行管理
        HomeManager homeManager = GameHelper.homeManager();
        //设置房间信息
        //  public HomeInfo(Player homeOwner, String max, Poker poker,String multiple,
        //String blind,String sharedIP,String AA,String method)
        HomeInfo homeInfo = new HomeInfo(ownPlayer, personNum, poker, multiple, blind, sharedIP, AA, method, roomNum);
        Home home = homeManager.createHome(homeInfo);
        ownPlayer.setChairId(home.getPlayers().size() - 1 + "");
        home.updatePlayer(ownPlayer);
        //返回六位房间号
        int hid = home.getHid();
        CreateRoomS2C_DTO createRoomS2CDto = CreateRoomS2C_DTO.builder().blind(blind).hid(hid + "")
                .ownerId(uid)
                .roomNum(roomNum).multiple(multiple)
                .AA(AA).method(method).sharedIP(sharedIP).currentRoomNum(roomNum).build();
        socketPackage.setDatagram(createRoomS2CDto);
        //返回创建成功
        ChannelFuture sync = null;
        try {
            sync = channel.writeAndFlush(socketPackage);
//            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sync.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    if (channelFuture.isSuccess()) {
                        //返回用户信息
                        PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                .chairId(ownPlayer.getChairId()).ip(userInfo.getIp()).gender(userInfo.getGender()).name(userInfo.getName()).iconurl(userInfo.getIconurl()).status(ownPlayer.getStatus()).uid(uid).build();
                        SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
                        channelFuture.channel().writeAndFlush(playerInfoSocket);
                    }
                }
            }
        });
        return socketPackage;
    }


    public SocketPackage leaveHome(SocketPackage socketPackage, Channel channel) {
        LeaveRoomC2S_DTO leaveRoomC2SDto = (LeaveRoomC2S_DTO) socketPackage.getDatagram();
        String leaveUid = leaveRoomC2SDto.getUid();
        String leaveHid = leaveRoomC2SDto.getHid();
        Home home = GameHelper.homeManager().getHome(leaveHid);
        if (home == null) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "未找到当前房间号:" + leaveHid);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        Player player = home.getPlayer(leaveUid);
        home.removePlayer(leaveUid);
        //当已准备的玩家,退出,减少退出的玩家
        if (player.getStatus().equalsIgnoreCase("2")) {
            home.subReady();
        }

        if (home.isExit()) {
            Home home1 = GameHelper.homeManager().clearHome(leaveHid);
            if (home1 != null) {
                System.err.println("当前房间号:" + leaveHid + " , 已经移除....");
            }
        }
        //TODO 通知其他用户
        socketPackage.setDatagram(new ResultDatagram());
        ChannelFuture channelFuture = channel.writeAndFlush(socketPackage);
//        MongoDao mongoDao = IOC.get().getBean(MongoDao.class);
        String query = String.format("{\"uid\":\"%s\"}", player.getUid());
        UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    if (!player.getChairId().equalsIgnoreCase("-1")) {
                        List<Player> players = home.getPlayers();
                        for (int i = 0; i < players.size(); i++) {
                            Player player1 = players.get(i);
                            Channel otherChannel = player1.getChannel();
                            //返回用户信息
                            PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                                    .chairId("-1").ip(userInfo.getIp()).gender(userInfo.getGender()).name(userInfo.getName()).iconurl(userInfo.getIconurl()).status("5").uid(player.getUid()).build();
                            SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
                            otherChannel.writeAndFlush(playerInfoSocket);
                        }
                    }
                }
            }
        });
        return socketPackage;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String hid = ChannelAttributeTools.attr(ctx.channel(), "hid");
        String uid = ChannelAttributeTools.attr(ctx.channel(), "uid");
        System.err.printf("房间号: %s, 玩家: %s ,断开连接", hid, uid);
        BreakConnectTools.addUidAndHid(uid, hid);
        ctx.channel().close();
    }
}
