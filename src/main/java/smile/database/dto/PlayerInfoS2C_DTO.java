package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: 当创建房间返回用户信息
 * @author: liuxin
 * @date: 2018/3/24 下午2:02
 */
@Data
@Builder
public class PlayerInfoS2C_DTO implements Datagram {
    private String uid;     //userId
    private String gender;       //性别
    private String name;      //名称
    private String iconurl;      //头像
    private String ip;      //ip
    private String chairId;      //玩家ID
    private String status; //
}
