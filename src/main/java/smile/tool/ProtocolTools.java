package smile.tool;

import org.smileframework.tool.serialization.SerializationTools;
import smile.protocol.*;
import smile.protocol.impl.GameProtocol;
import java.util.Arrays;

/**
 * @Package: com.netty
 * @Description:
 * @author: liuxin
 * @date: 2018/3/16 下午5:21
 */
public class ProtocolTools {


    /**
     * 是否需要对byte处理
     *
     * @param bytes
     * @return
     */
    public  SocketPackage unpack(byte[] bytes,ProtocolStrategy protocolStrategy) {
        return unpack(bytes, false,protocolStrategy);
    }

    /**
     * 查分协议头和数据包
     */
    public  SocketPackage unpack(byte[] bytes, boolean byteFlag,ProtocolStrategy protocolStrategy) {
        if (byteFlag) {
            //将bytes,从c转成java类型
        }
        if (bytes.length < 4) {
            throw new SocketConnectionException("This is an illegal connections, use less bytes 4");
        }
        byte[] head = new byte[4];
        byte[] data = new byte[bytes.length - 4];
        System.arraycopy(bytes, 0, head, 0, 4);
        Protocol protocol = null;
        try {
            protocol = SerializationTools.deserialize(head, GameProtocol.class);
        } catch (Exception e) {
            throw new ProtocolConvertException("协议头反序列化失败: "+ Arrays.asList(head));
        }
        Datagram datagram = null;
        if (bytes.length > 4) {
            System.arraycopy(bytes, 4, data, 0, bytes.length - 4);
            Class<? extends Datagram> datagramClass = protocolStrategy.strategy(protocol).getClass();
            datagram = SerializationTools.deserialize(data, datagramClass);
        }
        return new SocketPackage(protocol, datagram);
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[]{8, 0, 16, 0};
        SocketPackage socketPackage = new ProtocolTools().unpack(bytes,new GameProtocolStrategy());
        System.out.println(socketPackage.getProtocol().toString());
        System.out.println(socketPackage.getDatagram().toString());
    }


    public static void unparkTest() {
        byte[] bytes = new byte[]{8, 0, 16, 0, 14, 13};
        byte[] head = new byte[4];
        byte[] boby = new byte[bytes.length - 4];
        System.arraycopy(bytes, 0, head, 0, 4);
        System.arraycopy(bytes, 4, boby, 0, bytes.length - 4);
    }
}
