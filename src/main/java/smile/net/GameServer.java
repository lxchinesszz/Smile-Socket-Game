package smile.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.smileframework.tool.threadpool.SmileThreadFactory;
import smile.protocol.GameChannelDispatchHandler;
import smile.protocol.GameProtocolStrategy;
import smile.protocol.GameV2ProtocolStrategy;
import smile.serialize.MessageEncoder;
import smile.serialize.MessagesDecoder;

public class GameServer {

    public static void start(int port) throws Exception {
        GameV2ProtocolStrategy v2ProtocolStrategy=new GameV2ProtocolStrategy();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 通过nio方式来接收连接和处理连接
        EventLoopGroup group = new NioEventLoopGroup(0, new SmileThreadFactory("game-group"));
        EventLoopGroup work = new NioEventLoopGroup(0, new SmileThreadFactory("game-work"));
        /**
         *  TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，
         *  就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，
         *  就设置为false等累积一定大小后再发送。默认为false。
         */
        /**
         * 这个都是socket的标准参数，并不是netty自己的。
         * 具体为：

         * ChannelOption.SO_BACKLOG, 1024
         * BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
         * 如果未设置或所设置的值小于1，Java将使用默认值50。

         * ChannelOption.SO_KEEPALIVE, true
         * 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，
         * 这套机制才会被激活。

         * ChannelOption.TCP_NODELAY, true
         * 在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。
         * 为了尽可能的利用网络带宽，TCP总是希望尽可能的发送足够大的数据。
         * 这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。
         * TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；
         * 如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
         */
        serverBootstrap.group(group, work).childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
//                new GameProtocolStrategy())
                pipeline.addLast("decode", new MessagesDecoder(v2ProtocolStrategy));
                pipeline.addLast("encode", new MessageEncoder());
                pipeline.addLast("handler", new GameChannelDispatchHandler());
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
