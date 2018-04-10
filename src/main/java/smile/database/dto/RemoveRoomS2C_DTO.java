package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/9 下午11:29
 */
@Data
@Builder
public class RemoveRoomS2C_DTO implements Datagram {
    private String code;
}

