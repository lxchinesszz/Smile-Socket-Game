package smile.protocol.impl;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.protocol.impl
 * @Description: 服务令牌
 * @author: liuxin
 * @date: 2018/3/26 下午10:11
 */
@Data
public class ServiceTokenDatagram implements Datagram {
    private String serviceToken;
}
