package smile.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.smileframework.tool.common.DateFormatTools;
import org.smileframework.tool.common.pool.ObjectPool;
import org.smileframework.tool.common.pool.ObjectPoolBuilder;
import smile.protocol.ProtocolStrategy;
import smile.protocol.SocketPackage;
import smile.tool.ProtocolTools;

import java.net.SocketAddress;
import java.util.List;

/**
 * 添加拆包
 */
public class MessagesDecoder extends ByteToMessageDecoder {

    private ProtocolStrategy protocolStrategy;

    private ObjectPool<ProtocolTools> protocolToolsObjectPool;

    public MessagesDecoder(ProtocolStrategy protocolStrategy) {
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
        int beginReader;
        byte[] arr = null;
        if (!byteBuf.isReadable()){
            Channel channel = channelHandlerContext.channel();
            SocketAddress socketAddress = channel.remoteAddress();
            channel.close();
            System.err.println(">>>>>>>>>["+socketAddress+"]客户端已主动断开连接....");
            return;
        }
        while (true) {
            // 标记包头开始的index
            byteBuf.markReaderIndex();
            // 获取包头开始的index
            beginReader = byteBuf.readerIndex();
            // 消息的长度
            int length = byteBuf.readInt();
            // 判断请求数据包数据是否到齐
            byteBuf.resetReaderIndex();
            // 收到数据总共长度
            int i1 = byteBuf.readableBytes();
            //判断数据包是否到齐
            if ( i1< length + 4) {
                // 还原读指针
                byteBuf.readerIndex(beginReader);
                return;
            }
            // 读取data数据
            byteBuf.readerIndex(beginReader+4);
            arr= new byte[length];
            byteBuf.readBytes(arr,0,length);
            break;
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
