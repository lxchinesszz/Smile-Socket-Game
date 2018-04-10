package smile.service.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.config.Constants;
import smile.database.mongo.MongoDao;
import smile.global.annotation.ActionMapping;
import smile.protocol.SocketPackage;
import smile.protocol.impl.UserDatagram;
import smile.tool.ChannelAttributeTools;

@SmileComponent
@ActionMapping(sub = 1)
public class LoginAction extends Action {

    @InsertBean
    MongoDao mongoDao;

    @Override
    SocketPackage operator(SocketPackage socketPackage, ChannelHandlerContext ctx) {
        String ip = ChannelAttributeTools.attr(ctx.channel(), Constants.IP_KEY);
        Channel channel = ctx.channel();
        UserDatagram datagram = (UserDatagram) socketPackage.getDatagram();
        datagram.setIp(ip);
        socketPackage.setDatagram(datagram);
        String uid = datagram.getUid();
        String query = String.format("{\"uid\":\"%s\"}", uid);
        UserDatagram userDatagram = mongoDao.findOne(query, UserDatagram.class);
        if (userDatagram == null) {
            datagram.setCardNum("5");
            mongoDao.insert(datagram);
            socketPackage.setDatagram(datagram);
        } else {
            socketPackage.setDatagram(userDatagram);
        }
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }
}
