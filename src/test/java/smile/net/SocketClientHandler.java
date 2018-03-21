package smile.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Package: smile.net
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/20 下午9:48
 */
public class SocketClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
        // TODO Auto-generated method stub
        String data = msg.toString();
        System.out.println("数据内容：data="+data);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, String data) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("数据内容：data="+data);
    }
}