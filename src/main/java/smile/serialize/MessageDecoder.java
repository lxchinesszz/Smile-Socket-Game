package smile.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.LoggerFactory;
import org.smileframework.tool.common.DateFormatTools;
import org.smileframework.tool.common.pool.ObjectPool;
import org.smileframework.tool.common.pool.ObjectPoolBuilder;
import org.smileframework.tool.logmanage.LoggerManager;
import smile.protocol.ProtocolStrategy;
import smile.protocol.SocketPackage;
import smile.tool.ListTools;
import smile.tool.ProtocolTools;

import java.net.SocketAddress;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    private ProtocolStrategy protocolStrategy;

    private ObjectPool<ProtocolTools> protocolToolsObjectPool;

    public MessageDecoder(ProtocolStrategy protocolStrategy) {
        this.protocolStrategy = protocolStrategy;
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(2);
        config.setMinEvictableIdleTimeMillis(123);
        config.setMaxTotal(2);
        config.setMaxWaitMillis(-1);
        this.protocolToolsObjectPool=new ObjectPoolBuilder().setObject(new ProtocolTools()).setConfig(config).create();
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        ByteBuf directBuf = byteBuf;
        byte[] arr = null;
        if (!directBuf.hasArray()) {
            int len = directBuf.readableBytes();
            arr = new byte[len];
            directBuf.getBytes(0, arr);
        }
          if (arr==null){
            Channel channel = channelHandlerContext.channel();
            SocketAddress socketAddress = channel.remoteAddress();
            channel.close();
            System.err.println(">>>>>>>>>["+socketAddress+"]客户端已主动断开连接....");
            return;
        }
        ProtocolTools protocolTools = protocolToolsObjectPool.borrowObject();
        //从对象池中获取
        SocketPackage socketPackage = protocolTools.unpack(arr, protocolStrategy);
        list.add(socketPackage);
        if (socketPackage.getProtocol().getMain()==0&&socketPackage.getProtocol().getSub()==0){

        }else {
            System.out.println(">->->->>->->->>->->->>-"+ DateFormatTools.getDateFormat("HH:mm:ss")+">->->>->->->>->->->>->->->>->->->");
            System.out.printf("入口反序列化: %s",socketPackage.toString());
            System.out.println();
            System.out.println(">->->->>->->->>->->->>-"+ DateFormatTools.getDateFormat("HH:mm:ss")+">->->>->->->>->->->>->->->>->->->");
        }
         byteBuf.skipBytes(byteBuf.readableBytes());
        protocolToolsObjectPool.returnObject(protocolTools);
    }
}
