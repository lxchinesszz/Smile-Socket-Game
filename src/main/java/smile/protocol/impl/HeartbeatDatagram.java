package smile.protocol.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.protocol.impl
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/19 下午10:34
 */
@Data
public class HeartbeatDatagram implements Datagram {
    private boolean live;

    public HeartbeatDatagram() {
        this(true);
    }
    public HeartbeatDatagram(boolean live) {
        this.live = live;
    }
}
