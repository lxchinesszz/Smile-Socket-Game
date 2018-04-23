package smile.protocol;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import smile.config.Constants;
import smile.tool.*;

import java.net.InetSocketAddress;
import java.util.*;


/**
 * 核心处理类:
 * <p>
 * 主要负责Channel的业务分发
 */
public class GameChannelDispatchHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("----------------------连接成功------------------");
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = address.getHostName();
        String sessionId = UUID.randomUUID().toString().toUpperCase();
        ChannelAttributeTools.attr(ctx.channel(), Constants.SESSION_KEY, sessionId);
        ChannelAttributeTools.attr(ctx.channel(), Constants.IP_KEY, ip);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketPackage socketPackage = (SocketPackage) msg;
        int sub = socketPackage.getProtocol().getSub();
        Channel channel = ctx.channel();
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                ActionTools.opera((byte) sub, socketPackage, channel);
            }
        });
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String hid = ChannelAttributeTools.attr(ctx.channel(), "hid");
        String uid = ChannelAttributeTools.attr(ctx.channel(), "uid");
        String token = ChannelAttributeTools.attr(ctx.channel(), "token");
        System.err.printf("房间号: %s, 玩家: %s ,断开连接", hid, uid);
        BreakConnectTools.addUidAndHid(uid, hid);
        ctx.channel().close();
    }

}
