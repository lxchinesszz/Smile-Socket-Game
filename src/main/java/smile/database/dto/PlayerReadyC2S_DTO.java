package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: 准备
 * @author: liuxin
 * @date: 2018/3/24 下午3:18
 */
@Data
public class PlayerReadyC2S_DTO implements Datagram{
    private String uid;
    private String hid;
}
