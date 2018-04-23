package smile.service.handler;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.bson.Document;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.database.domain.NotifiyEntity;
import smile.database.domain.UserEntity;
import smile.database.dto.CardNotifyS2C_DTO;
import smile.database.dto.NotifiyS2C_DTO;
import smile.database.dto.OperatorS2C_DTO;
import smile.database.dto.PlayerInfoS2C_DTO;
import smile.database.mongo.MongoDao;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.protocol.impl.UserDatagram;
import smile.service.home.Home;
import smile.service.home.Player;
import smile.tool.DateTools;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @Package: smile.service.handler
 * @Description:
 * @author: liuxin
 * @date: 2018/3/27 上午11:35
 */
@SmileComponent
public class PlayerInfoNotify {

    @InsertBean
    MongoDao mongoDao;


    public SocketPackage gameNotify(Channel channel){
        Document query2 = new Document("endTime", new Document("$gt", System.currentTimeMillis()));
        List<NotifiyEntity> all = mongoDao.findAll(query2.toJson(),NotifiyEntity.class);
        SocketPackage socketPackage = new SocketPackage(new Protocol(2, 21));
        if (all!=null&all.size()>0){
            socketPackage.setDatagram(new NotifiyS2C_DTO("0",all));
        }else {
            socketPackage.setDatagram(new NotifiyS2C_DTO("-1",all));
        }
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }

    /**
     * @param home     房间
     * @param leaveUid 离开人的id
     */
    public void leaveHomeNotify(Home home, String leaveUid) {
        UserEntity userInfo = mongoDao.findByUid(leaveUid, UserEntity.class);
        List<Player> players = home.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player1 = players.get(i);
            Channel otherChannel = player1.getChannel();
            //返回用户信息
            PlayerInfoS2C_DTO playerInfoS2CDto = PlayerInfoS2C_DTO.builder()
                    .chairId("-1").ip(userInfo.getIp())
                    .gender(userInfo.getGender())
                    .name(userInfo.getName())
                    .iconurl(userInfo.getIconurl())
                    .status("5").uid(leaveUid).build();
            SocketPackage playerInfoSocket = new SocketPackage(new Protocol(2, 9), playerInfoS2CDto);
            otherChannel.writeAndFlush(playerInfoSocket);
        }
    }

    public void operator(Home home, SocketPackage socketPackage) {
        List<Player> players = home.getPlayers();
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            Player player = players.get(i);
            Channel channel = player.getChannel();
            channel.writeAndFlush(socketPackage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void operator(List <Player> players, SocketPackage socketPackage) {
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            Player player = players.get(i);
            Channel channel = player.getChannel();
            channel.writeAndFlush(socketPackage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * 房卡推送
     * @param
     */
    public void notifyCardNum(List<Player> players){
        SocketPackage socketPackage=new SocketPackage(new Protocol(2,19));
        for (Player player:players){
            Channel channel = player.getChannel();
            if (channel!=null){
                String uid = player.getUid();
                UserDatagram byUid = mongoDao.findByUid(uid, UserDatagram.class);
                socketPackage.setDatagram(new CardNotifyS2C_DTO(uid,byUid.getCardNum()));
                ChannelFuture channelFuture =
                        channel.writeAndFlush(socketPackage);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()){
                            System.err.println("玩家:"+player+byUid+".房卡信息已推送");
                        }
                    }
                });
            }
        }
    }


    /**
     * 房卡推送
     * @param home
     */
    public void notifyCardNum(Home home){
        List<Player> players = home.getPlayers();
        SocketPackage socketPackage=new SocketPackage(new Protocol(2,19));
        for (Player player:players){
            Channel channel = player.getChannel();
            if (channel!=null){
                String uid = player.getUid();
                UserEntity byUid = mongoDao.findByUid(uid, UserEntity.class);
                socketPackage.setDatagram(new CardNotifyS2C_DTO(uid,byUid.getCardNum()));
                channel.writeAndFlush(socketPackage);
            }
        }
    }

    /**
     * 信息推送，排除指定uid
     * @param home
     * @param socketPackage
     * @param uid
     */
    public void operatorByUid(Home home, SocketPackage socketPackage,String uid) {
        List<Player> players = home.getPlayers();
        Iterator<Player> iterator = players.stream().filter(new Predicate<Player>() {
            @Override
            public boolean test(Player player) {
                return !player.getUid().equalsIgnoreCase(uid);
            }
        }).iterator();
        ArrayList<Player> players0 = Lists.newArrayList(iterator);
        for (int i = 0, lenth = players0.size(); i < lenth; i++) {
            Player player = players0.get(i);
            Channel channel = player.getChannel();
            channel.writeAndFlush(socketPackage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}

