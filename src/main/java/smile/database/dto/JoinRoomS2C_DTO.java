package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/24 上午12:13
 */
@Data
@Builder
public class JoinRoomS2C_DTO implements Datagram{
    private String hid; //房间号码
    private String ownerId; //房间拥有者
    private String roomNum;  //房间总局数
    private String blind;    //底注
}
