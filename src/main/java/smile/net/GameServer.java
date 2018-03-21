package smile.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.smileframework.tool.threadpool.SmileThreadFactory;
import smile.protocol.GameChannelHandler;
import smile.protocol.GameProtocolStrategy;
import smile.serialize.MessageDecoder;
import smile.serialize.MessageEncoder;

public class GameServer {

    public static void start(int port) throws Exception {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 通过nio方式来接收连接和处理连接
        EventLoopGroup group = new NioEventLoopGroup(0, new SmileThreadFactory("game-group"));
        EventLoopGroup work = new NioEventLoopGroup(0, new SmileThreadFactory("game-work"));
        serverBootstrap.group(group, work).channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decode",new MessageDecoder(new GameProtocolStrategy()));
                pipeline.addLast("encode",new MessageEncoder());
                pipeline.addLast("handler",new GameChannelHandler());
            }
        });
        try {
            ChannelFuture sync = serverBootstrap.bind(port).sync();
            sync.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("服务器绑定成功");
                    }
                }
            });
        } catch (InterruptedException e) {
            work.shutdownGracefully();
            group.shutdownGracefully();
        }


    }
}
