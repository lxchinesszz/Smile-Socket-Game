package smile.protocol;

import smile.protocol.impl.GameProtocol;

/**
 * @Package: com.netty
 * @Description: 协议头和数据报
 * @author: liuxin
 * @date: 2018/3/16 下午5:25
 */
public class SocketPackage {

    private Protocol protocol;

    private Datagram datagram;

    public SocketPackage(Protocol protocol) {
        this(protocol, null);
    }

    public SocketPackage(Protocol protocol, Datagram datagram) {
        this.protocol = protocol;
        this.datagram = datagram;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Datagram getDatagram() {
        return datagram;
    }

    public void setDatagram(Datagram datagram) {
        this.datagram = datagram;
    }

    @Override
    public String toString() {
        return "SocketPackage{" +
                "protocol=" + protocol +
                ", datagram=" + datagram +
                '}';
    }
}
