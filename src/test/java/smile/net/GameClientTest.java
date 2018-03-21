package smile.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import junit.framework.TestCase;
import org.smileframework.tool.serialization.SerializationTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Package: smile.net
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/20 上午9:52
 */
public class GameClientTest extends TestCase {
    public static String host = "127.0.0.1";  //ip地址
    public static int port = 10101;          //端口
    /// 通过nio方式来接收连接和处理连接
    private static EventLoopGroup group = new NioEventLoopGroup();
    private static Bootstrap b = new Bootstrap();

    /**
     * Netty创建全部都是实现自AbstractBootstrap。
     * 客户端的是Bootstrap，服务端的则是    ServerBootstrap。
     **/
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("客户端成功启动...");
        b.group(group);
        b.channel(NioSocketChannel.class);
       b.handler(new ChannelInitializer() {
           @Override
           protected void initChannel(Channel ch) throws Exception {
               ChannelPipeline pipeline = ch.pipeline();
                /*
                 * 这个地方的 必须和服务端对应上。否则无法正常解码和编码
                 *
                 * 解码和编码 我将会在下一张为大家详细的讲解。再次暂时不做详细的描述
                 *
                 * */
//               pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
               pipeline.addLast("decoder", new StringDecoder());
               pipeline.addLast("encoder", new StringEncoder());
               pipeline.addLast(new SocketClientHandler());
           }
       });
        // 连接服务端
        ChannelFuture channelFuture = b.connect(host, port).sync();
        if (channelFuture.isSuccess()){
            channelFuture.channel().writeAndFlush("你好");
        }
        while (true){

        }
    }


}