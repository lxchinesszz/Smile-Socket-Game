package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/23 下午10:40
 */
@Data
public class CreateRoomC2S_DTO implements Datagram {
    private String uid;
    private String roomNum;  //房间总局数
    private String multiple; //最大倍数
    private String personNum; //房间人数
    private String blind;    //底注
    private String sharedIP; //是否共享玩家IP  1=是  0=不是
    private String AA;//房主出房卡还是AA 房主出 = 0 AA= 1
    private String method;//玩发
}
