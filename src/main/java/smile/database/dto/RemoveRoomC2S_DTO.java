package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/9 下午11:29
 */
@Data
public class RemoveRoomC2S_DTO implements Datagram {
    private String hid;
    private String uid;
}

