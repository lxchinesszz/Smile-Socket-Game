package smile.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.smileframework.tool.serialization.SerializationTools;
import smile.protocol.Datagram;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.tool.ListTools;

import java.util.ArrayList;
import java.util.List;


/**
 * @Package: smile.serialize
 * @Description:
 * @author: liuxin
 * @date: 2018/3/20 上午10:16
 */
public class MessageEncoder extends MessageToByteEncoder<SocketPackage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, SocketPackage socketPackage, ByteBuf byteBuf) throws Exception {
        Protocol protocol = socketPackage.getProtocol();
        protocol.setMain((byte) 2);
        byte[] protocolVersion = SerializationTools.serialize(protocol);
        Datagram datagram = socketPackage.getDatagram();
        byte[] datagramBody = SerializationTools.serialize(datagram);
        byte[] body = new byte[protocolVersion.length + datagramBody.length];
        System.arraycopy(protocolVersion,0,body,0,protocolVersion.length);
        System.arraycopy(datagramBody,0,body,protocolVersion.length,datagramBody.length);
        byteBuf.writeBytes(body);
    }
}
