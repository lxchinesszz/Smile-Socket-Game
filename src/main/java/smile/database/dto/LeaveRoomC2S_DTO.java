package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: liuxin
 * @date: 2018/3/24 下午1:12
 */
@Data
public class LeaveRoomC2S_DTO implements Datagram{
   private String uid;  //用户ID
   private String hid;  //房间ID
}
