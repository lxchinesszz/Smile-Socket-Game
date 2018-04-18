package smile.protocol.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import smile.config.Table;
import smile.protocol.Datagram;

/**
 * @Package: com.netty
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/16 下午5:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "ddz_user")
public class UserDatagram implements Datagram {
    /**
     * 用户id
     */
    private String uid;
    /**
     * 性别
     */
    private String gender;
    /**
     * 要有校验规则
     * 昵称
     */
    private String name;
    /**
     * 用户token
     */
    private String accessToken;
    /**
     * 用户头像
     */
    private String iconurl;

    private String ip;

    private String cardNum;
    /**
     * 是否是供应商
     */
    private String isAdmin;
    /**
     * 房间号
     */
    private String hid;
    /**
     * 剩余牌局书
     */
    private String shengyuRoomNum;

    /**
     * 是否断线重连
     */
    private String isBreakConnect;
}
