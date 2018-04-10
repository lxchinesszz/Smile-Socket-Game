package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: 加入房间信息
 * @author: liuxin
 * @date: 2018/3/24 上午12:13
 */
@Data
public class JoinRoomC2S_DTO implements Datagram {
    private String hid;
    private String uid;
}
