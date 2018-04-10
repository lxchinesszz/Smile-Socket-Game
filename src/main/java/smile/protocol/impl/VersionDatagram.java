package smile.protocol.impl;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.protocol.impl
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/26 下午10:16
 */
@Data
public class VersionDatagram implements Datagram {
    private String version;

    public VersionDatagram() {
    }

    public VersionDatagram(String version) {
        this.version = version;
    }
}
