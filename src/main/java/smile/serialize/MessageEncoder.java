package smile.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.smileframework.tool.common.DateFormatTools;
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
        int length = protocolVersion.length + datagramBody.length;
        System.arraycopy(protocolVersion, 0, body, 0, protocolVersion.length);
        System.arraycopy(datagramBody, 0, body, protocolVersion.length, datagramBody.length);

        if (socketPackage.getProtocol().getMain() == 2 && socketPackage.getProtocol().getSub() == 0) {

        } else {
            System.out.println(">->->->>->->->>->->->>-" + DateFormatTools.getDateFormat("HH:mm:ss") + ">->->>->->->>->->->>->->->>->->->");
            System.out.printf("出口序列化: %s,长度:%d", socketPackage.toString(), length+4);
            System.out.println();
            System.out.println(">->->->>->->->>->->->>-" + DateFormatTools.getDateFormat("HH:mm:ss") + ">->->>->->->>->->->>->->->>->->->");
            //将长度放在前面
        }
        byteBuf.writeInt(length + 4);
        byteBuf.writeBytes(body);

    }

    public static void main(String[] args) {
        byte[] bytes = IntToByteArray(78);
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(bytes[i] + ",");
        }

        System.out.println();
        byte[] b = new byte[4];
        b[0] = 0;
        b[1] = 0;
        b[2] = 0;
        b[3] = 78;
        System.out.println(ByteArrayToInt(b));
    }


    public static byte[] IntToByteArray(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static int ByteArrayToInt(byte[] bArr) {
        if (bArr.length != 4) {
            return -1;
        }
        return (int) ((((bArr[3] & 0xff) << 24)
                | ((bArr[2] & 0xff) << 16)
                | ((bArr[1] & 0xff) << 8)
                | ((bArr[0] & 0xff) << 0)));
    }
}
