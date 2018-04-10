package smile.service.handler;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.database.domain.UserEntity;
import smile.database.dto.OperatorS2C_DTO;
import smile.database.dto.PlayerInfoS2C_DTO;
import smile.database.mongo.MongoDao;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.service.home.Home;
import smile.service.home.Player;

import java.util.ArrayList;
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

