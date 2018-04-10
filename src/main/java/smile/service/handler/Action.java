package smile.service.handler;

import io.netty.channel.ChannelHandlerContext;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.protocol.SocketPackage;

/**
 * @Package: smile.service.handler
 * @Description:
 * @author: liuxin
 * @date: 2018/3/30 下午4:47
 */
public abstract class Action {
    abstract SocketPackage operator(SocketPackage socketPackage, ChannelHandlerContext ctx);
}
