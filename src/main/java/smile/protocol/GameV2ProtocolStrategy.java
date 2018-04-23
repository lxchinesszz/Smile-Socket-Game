package smile.protocol;

import smile.protocol.impl.HeartbeatDatagram;
import smile.tool.ActionTools;


/**
 * @Package: smile.protocol
 * @Description: 自动从注解中获取到序列化的模型信息
 * @date: 2018/4/23 上午1:37
 * @author: liuxin
 */
public class GameV2ProtocolStrategy implements ProtocolStrategy {
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
        ActionTools.ActionModel actionHandler = ActionTools.getActionHandler((byte) sub);
        Object datagram = null;
        try {
            datagram = actionHandler.getSerialiModel().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return ((Datagram) datagram);
    }
}
