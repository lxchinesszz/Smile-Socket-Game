package smile.service.handler;

import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.SocketPackage;
import smile.protocol.impl.HeartbeatDatagram;

/**
 * @Package: smile.service.handler
 * @Description: 心跳
 * @date: 2018/4/17 上午12:42
 * @author: liuxin
 */
@SmileComponent
@Action
public class HeartbeatActionHandler {
    @SubOperation(sub = 0)
    public SocketPackage hearbeat(SocketPackage socketPackage, Channel channel){
        socketPackage.setDatagram(new HeartbeatDatagram());
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }
}
