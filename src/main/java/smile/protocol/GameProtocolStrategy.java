package smile.protocol;

import smile.database.dto.*;
import smile.protocol.impl.HeartbeatDatagram;
import smile.protocol.impl.ServiceTokenDatagram;
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
        if (main > 2 || main < 0) {
            throw new RuntimeException("客户端请求协议有误,主协议号:[ " + main + " ],副号:[ " + sub + " ]");
        }
        if (main == 0 && sub == 0) {
            //心跳包
            return new HeartbeatDatagram();
        }
        if (sub == 1) {
            return new UserDatagram();
        } else if (sub == 3) {
            return new CreateRoomC2S_DTO();
        } else if (sub == 4) {
            return new JoinRoomC2S_DTO();
        } else if (sub == 5) {
            return new LeaveRoomC2S_DTO();
        } else if (sub == 10) {
            return new PlayerReadyC2S_DTO();
        } else if (sub == 100) {
            return new ServiceTokenDatagram();
        } else if (sub == 12) {
            return new OperatorC2S_DTO();
        } else if (sub == 14) {
            return new CheckPokerC2S_DTO();
        } else if (sub == 16) {
            return new RemoveRoomC2S_DTO();
        } else if (sub == 17) {
            return new ChatC2S_DTO();
        } else if (sub == 18) {
            return new ActiveC2S_DTO();
        } else if (sub == 20) {
            return new UserC2S_DTO();
        } else if (sub == 22) {
            return new ChongZhiC2S_DTO();
        } else if (sub == 23) {
            return new UserC2S_DTO();
        } else if (sub == 24) {
            return new UserC2S_DTO();
        }else if (sub==25){
            return new UserDatagram();
        }else if (sub==26){
            return new UserDatagram();
        }
        return new UserDatagram();
    }
}
