package smile.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Package: smile.net
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/20 下午9:48
 */
public class SocketClientHandler extends ChannelInboundHandlerAdapter {

    private final String echo_req = "hi,!@#$@#@SADAS," +
            "!_#_$$__$$__&$";

    @Override
    public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO Auto-generated method stub
        for (int i = 0; i < 10; i++) {
            ctx.channel().writeAndFlush(echo_req);
        }
    }


}