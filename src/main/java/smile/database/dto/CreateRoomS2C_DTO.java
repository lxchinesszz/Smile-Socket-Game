package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/23 下午11:21
 */
@Data
@Builder
public class CreateRoomS2C_DTO implements Datagram {
    private String hid; //房间号码
    private String ownerId; //房间拥有者
    private String roomNum;  //房间总局数
    private String blind;    //底注
    private String sharedIP; //是否共享玩家IP  1=是  0=不是
    private String AA;//房主出房卡还是AA 房主出 = 0 AA= 1
    private String method;//玩发
    private String currentRoomNum;//剩余局数
    private String multiple; //最大倍数
    public CreateRoomS2C_DTO(){}

    public CreateRoomS2C_DTO(String hid, String ownerId, String roomNum, String blind, String sharedIP, String AA, String method, String currentRoomNum, String multiple) {
        this.hid = hid;
        this.ownerId = ownerId;
        this.roomNum = roomNum;
        this.blind = blind;
        this.sharedIP = sharedIP;
        this.AA = AA;
        this.method = method;
        this.currentRoomNum = currentRoomNum;
        this.multiple = multiple;
    }
}
