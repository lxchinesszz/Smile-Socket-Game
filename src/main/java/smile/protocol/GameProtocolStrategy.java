package smile.protocol;

import smile.protocol.impl.HeartbeatDatagram;
import smile.protocol.impl.UserDatagram;

/**
 * @Package: smile.protocol
 * @Description:
 * @author: liuxin
 * @date: 2018/3/21 上午10:50
 */
public class GameProtocolStrategy implements ProtocolStrategy {
    @Override
    public Datagram strategy(Protocol protocol) {
        /**
         * 主号
         */
        int main = protocol.getMain();
        /**
         * 副号
         */
        int sub = protocol.getSub();
        if (main>2||main<0){
            throw new RuntimeException("客户端请求协议有误,主协议号:[ "+main+" ],副号:[ "+sub+" ]");
        }
        if (main==0&&sub==0){
            //心跳包
            return new HeartbeatDatagram();
        }
        if (sub==1){
            return new UserDatagram();
        }
        return new UserDatagram();
    }
}
