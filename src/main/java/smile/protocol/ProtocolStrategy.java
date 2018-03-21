package smile.protocol;

/**
 * @Package: smile.protocol
 * @Description: 协议策略类
 * 根据协议号,拿到不同的数据体
 * @author: liuxin
 * @date: 2018/3/21 上午10:48
 */
public interface ProtocolStrategy {
    Datagram strategy(Protocol protocol);
}
