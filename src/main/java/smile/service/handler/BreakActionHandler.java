package smile.service.handler;

import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.database.domain.UserEntity;
import smile.database.dto.BreakConnectS2C_DTO;
import smile.database.dto.CreateRoomS2C_DTO;
import smile.database.dto.PlayerInfoS2C_DTO;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.protocol.impl.UserDatagram;
import smile.service.home.Home;
import smile.service.home.HomeInfo;
import smile.service.home.Player;
import smile.service.poker.Card;
import smile.tool.BreakConnectTools;
import smile.tool.GameHelper;
import smile.tool.IOC;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: smile.service.handler
 * @Description: 断线重连
 * @author: mac
 * @date: 2018/4/16 下午5:10
 */
@Action
@SmileComponent
public class BreakActionHandler extends AbstractActionHandler {

    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;
    /**
     * 断线重连
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 25)
    public SocketPackage breakCollection(SocketPackage socketPackage, Channel channel) {
        UserDatagram datagram = (UserDatagram) socketPackage.getDatagram();
        String uid0 = datagram.getUid();
        String hid = BreakConnectTools.getHidByUid(uid0);
        Home home = GameHelper.homeManager().getHome(hid);
        Player player2 = home.getPlayer(uid0);
        player2.setChannel(channel);
        //TODO 房间里自己的牌，和房间信息，和当前出的牌,其他玩家的信息
        List<Player> players = home.getPlayers();
        //TODO 获取当前桌面的牌
        List<Card> currentCards = home.getCurrentCards();
        ArrayList<Card> currentPlayerPoker = home.getPlayer(uid0).getPoker();
        BreakConnectS2C_DTO breakConnectS2C_dto = new BreakConnectS2C_DTO();
        //添加玩家剩余的牌
        breakConnectS2C_dto.addPokers(currentPlayerPoker);
        //当前桌面上的牌
        breakConnectS2C_dto.addCurrentPokers(currentCards);
        //当前地主
        breakConnectS2C_dto.setLandLordChairId(home.getLandLordChairId() + "");
        //当前玩家的数量
        breakConnectS2C_dto.addPokerCount(players);
        //底牌
        List<String> mainPokers = new ArrayList<>();
        home.getPoker().getMainPoker().stream().forEach(card -> {
            mainPokers.add(card.id + "");
        });
        breakConnectS2C_dto.setMainPokers(mainPokers);
        //当期操作的玩家
        //1. 已经出牌的情况
        if (home.getCurrentOutCardsPlayer() != null) {
            int currentOperaCharId = home.getNextOperaChairId(Integer.parseInt(home.getCurrentOutCardsPlayer().getChairId()));
            breakConnectS2C_dto.setCurrentOperaCharId(currentOperaCharId + "");
            //当期操作玩家的状态
            breakConnectS2C_dto.setCurrentOperaStatus(home.getPlayerByChairId(currentOperaCharId).getWillOperatorStatus() + "");
            //上一个操作玩家
            int prePlayerCharId = Integer.parseInt(home.getCurrentOutCardsPlayer().getChairId());
            breakConnectS2C_dto.setPreOperaCharId(String.valueOf(prePlayerCharId));
            breakConnectS2C_dto.setPreOperaStatus(home.getPlayerByChairId(prePlayerCharId).getOperatorStatus() + "");
        }else if(home.getCurrentChairId()!=-1){
            int currentChairId = home.getCurrentChairId();
            breakConnectS2C_dto.setCurrentOperaCharId(currentChairId + "");
            //当期操作玩家的状态
            breakConnectS2C_dto.setCurrentOperaStatus(home.getPlayerByChairId(currentChairId).getWillOperatorStatus() + "");
            //上一个操作玩家
            int preCharId=home.getPreOperaCharId(currentChairId);
            breakConnectS2C_dto.setPreOperaCharId(String.valueOf(preCharId));
            breakConnectS2C_dto.setPreOperaStatus(home.getPlayerByChairId(preCharId).getOperatorStatus() + "");
        } else {
            breakConnectS2C_dto.setCurrentOperaCharId(-1 + "");
            //当期操作玩家的状态
            breakConnectS2C_dto.setCurrentOperaStatus(-1 + "");
            //上一个操作玩家
            int prePlayerCharId = Integer.parseInt(-1 + "");
            breakConnectS2C_dto.setPreOperaCharId(String.valueOf(prePlayerCharId));
            breakConnectS2C_dto.setPreOperaStatus(-1 + "");
        }

        //房间信息包
        socketPackage.setProtocol(new Protocol(2, 4));
        socketPackage.setDatagram(createRoomS2C_dto(home.getHomeInfo(), hid));
        channel.writeAndFlush(socketPackage);
        //玩家信息包
        returnHomePlayerInfo(players, player2);
        //断线重连包
        socketPackage.setProtocol(new Protocol(2, 25));
        socketPackage.setDatagram(breakConnectS2C_dto);
        player2.getChannel().writeAndFlush(socketPackage);
        return socketPackage;
    }


    public SocketPackage returnHomePlayerInfo(List<Player> players, Player currentPlayer) {
        //TODO 返回房间信息
        for (int i = 0; i < players.size(); i++) {
            Player player1 = players.get(i);
            Channel otherChannel = player1.getChannel();
            String uid = currentPlayer.getUid();
            String query = String.format("{\"uid\":\"%s\"}", uid);
            UserEntity userInfo = mongoDao.findOne(query, UserEntity.class);
            //返回用户信息
            PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                    .chairId(currentPlayer.getChairId()).ip(userInfo.getIp()).gender(userInfo.getGender()).name(userInfo.getName()).iconurl(userInfo.getIconurl()).status(currentPlayer.getStatus()).uid(uid).build();
            SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
            otherChannel.writeAndFlush(playerInfoSocket);
        }
        Channel joinPlayer = currentPlayer.getChannel();
        //将所有人信息在通知给Player2
        for (int i = 0; i < players.size(); i++) {
            Player player1 = players.get(i);
            if (player1.getUid().equalsIgnoreCase(currentPlayer.getUid())) {
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
        return null;
    }

    /**
     * private String hid; //房间号码
     * private String ownerId; //房间拥有者
     * private String roomNum;  //房间总局数
     * private String blind;    //底注
     * private String sharedIP; //是否共享玩家IP  1=是  0=不是
     * private String AA;//房主出房卡还是AA 房主出 = 0 AA= 1
     * private String method;//玩发
     * private String currentRoomNum;//剩余局数
     * private String multiple; //最大倍数
     *
     * @param homeInfo
     */
    public CreateRoomS2C_DTO createRoomS2C_dto(HomeInfo homeInfo, String hid) {
        CreateRoomS2C_DTO createRoomS2C_dto = new CreateRoomS2C_DTO();
        createRoomS2C_dto.setHid(hid);
        createRoomS2C_dto.setOwnerId(homeInfo.getHomeOwner().getUid());
        createRoomS2C_dto.setRoomNum(homeInfo.getRoomNum());
        createRoomS2C_dto.setBlind(homeInfo.getBlind());
        createRoomS2C_dto.setSharedIP(homeInfo.getSharedIP());
        createRoomS2C_dto.setAA(homeInfo.getAA());
        createRoomS2C_dto.setMethod(homeInfo.getMethod());
        createRoomS2C_dto.setCurrentRoomNum(homeInfo.getRoomNum());
        createRoomS2C_dto.setMultiple(homeInfo.getMultiple());
        return createRoomS2C_dto;
    }



}
