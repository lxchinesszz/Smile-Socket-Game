package smile.service.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.string.StringTools;
import smile.config.Constants;
import smile.database.domain.HomeInfoEntity;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.SocketPackage;
import smile.protocol.impl.ServiceTokenDatagram;
import smile.protocol.impl.UserDatagram;
import smile.protocol.impl.VersionDatagram;
import smile.tool.BreakConnectTools;
import smile.tool.ChannelAttributeTools;
import smile.tool.IOC;
import smile.tool.StdRandom;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 登录方法
 */
@SmileComponent
@Action
public class LoginActionHandler extends AbstractActionHandler {
    private static final String TOKEN = "qwertyuio";
    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;

    /**
     * 连接验证
     * 1. 判断连接发送来的token是否与服务器一致，一致则可以登录，2.返回当前游戏版本号，否则拒绝连接
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 100)
    private SocketPackage serviceToken(SocketPackage socketPackage, Channel channel) {
        ServiceTokenDatagram serviceTokenDatagram = (ServiceTokenDatagram) socketPackage.getDatagram();
        //1.
        if (!TOKEN.equalsIgnoreCase(serviceTokenDatagram.getServiceToken())) {
            ChannelFuture close = channel.disconnect();
            close.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.err.println("已经拒绝连接.....");
                    }
                }
            });
        } else {
            //2.
            socketPackage.setDatagram(new VersionDatagram("1.0.0"));
            channel.writeAndFlush(socketPackage);
        }
        return socketPackage;
    }


    /**
     * 登录验证
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = {1})
    private SocketPackage operator(SocketPackage socketPackage, Channel channel) {
        String HOSTADDRESS = ChannelAttributeTools.attr(channel, Constants.IP_KEY);
        /**
         * 1. 判断该用户id是否已经注册,当已经注册,则直接返回
         * 2. 当未注册则,就直接保存
         */
        UserDatagram datagram = (UserDatagram) socketPackage.getDatagram();
        ChannelAttributeTools.attr(channel, "token", datagram.getAccessToken());
        String new_iconurl = datagram.getIconurl();
        datagram.setIp(HOSTADDRESS);
        socketPackage.setDatagram(datagram);
        String accessToken = datagram.getAccessToken();
        String query = String.format("{\"accessToken\":\"%s\"}", accessToken);
        UserDatagram userDatagram = mongoDao.findOne(query, UserDatagram.class);
        String uid = "";
        //2.
        if (userDatagram == null) {
            int i = StdRandom.generate6BitInt();
            uid = String.valueOf(i);
            datagram.setUid(String.valueOf(i));
            datagram.setCardNum("5");
            datagram.setIsAdmin("0");
            datagram.setHid("");
            datagram.setShengyuRoomNum("");
            mongoDao.insert(datagram);
            //TODO 判断是否登录
            String hid = BreakConnectTools.getHidByUid(uid);
            if (!StringTools.isBlank(hid)) {
                datagram.setIsBreakConnect("1");
            } else {
                datagram.setIsBreakConnect("0");
            }
            socketPackage.setDatagram(datagram);
        } else {
            mongoDao.del(query, "ddz_user");
            uid = userDatagram.getUid();
            if (StringTools.isEmpty(userDatagram.getUid())) {
                int i = StdRandom.generate6BitInt();
                uid = String.valueOf(i);
                userDatagram.setUid(String.valueOf(i));
            }
            //更新用户头像
            userDatagram.setIconurl(new_iconurl);
            //是否为代理商，默认非代理商
            userDatagram.setIsAdmin("0");
            mongoDao.insert(userDatagram);
            /**
             * 登录时候判断是否用户是断线重连
             */
            String hid = BreakConnectTools.getHidByUid(uid);
            if (!StringTools.isBlank(hid)) {
                //返回断线重连
                userDatagram.setIsBreakConnect("1");
            } else {
                //不是断线重连
                userDatagram.setIsBreakConnect("0");
            }
            socketPackage.setDatagram(userDatagram);
            //判断是否还有为大完的牌局数，如果存在多个，则返回最大的剩余牌局数
            String query0 = String.format("{\"ownerId\":\"%s\"}", userDatagram.getUid());
            List<HomeInfoEntity> all = mongoDao.findAll(query0, HomeInfoEntity.class);
            if (all != null & all.size() != 0) {
                Collections.sort(all, new Comparator<HomeInfoEntity>() {
                    @Override
                    public int compare(HomeInfoEntity o1, HomeInfoEntity o2) {
                        return o1.getShengyuRoomNum() - o2.getShengyuRoomNum();
                    }
                });
                userDatagram.setShengyuRoomNum(all.get(0).getShengyuRoomNum() + "");
                userDatagram.setHid(all.get(0).getHid());
                socketPackage.setDatagram(userDatagram);
            } else {
                userDatagram.setShengyuRoomNum("");
                userDatagram.setHid("");
            }
        }
        channel.writeAndFlush(socketPackage);
        //广告通知
        playerInfoNotify.gameNotify(channel);
        return socketPackage;
    }
}
