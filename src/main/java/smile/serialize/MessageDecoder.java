package smile.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.smileframework.tool.common.pool.ObjectPool;
import org.smileframework.tool.common.pool.ObjectPoolBuilder;
import smile.protocol.ProtocolStrategy;
import smile.protocol.SocketPackage;
import smile.tool.ProtocolTools;
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
        ProtocolTools protocolTools = protocolToolsObjectPool.borrowObject();
        //从对象池中获取
        SocketPackage socketPackage = protocolTools.unpack(arr, protocolStrategy);
        list.add(socketPackage);
        protocolToolsObjectPool.returnObject(protocolTools);
    }
}
