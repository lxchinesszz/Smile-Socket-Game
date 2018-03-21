package smile.protocol;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class GameChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接成功");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketPackage socketPackage = (SocketPackage) msg;
//        System.out.println(socketPackage);
        System.out.println(socketPackage);
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(socketPackage);
        System.out.println(channelFuture.isSuccess());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
