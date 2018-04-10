package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/24 下午3:20
 */
@Data
@Builder
public class PlayerStatusS2C_DTO implements Datagram {
    String uid;
    String chairId;
    String status;
}
