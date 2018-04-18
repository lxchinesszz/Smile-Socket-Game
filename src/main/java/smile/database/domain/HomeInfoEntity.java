package smile.database.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import smile.config.Table;
import smile.service.home.HomeInfo;
import smile.service.home.Player;
import smile.service.home.Poker;

/**
 * @Package: smile.database.domain
 * @Description: 当房间中没有玩家，房主并没有解散房间，而是离开房间，则保存房间信息到数据库
 * @author: mac
 * @date: 2018/4/14 下午6:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "ddz_home_info")
public class HomeInfoEntity {
    /**
     * 房主id
     */
    private String ownerId;
    /**
     * 房间号
     */
    private String hid;
    /**
     * 最大房间人数
     */
    private int personNum;


    /**
     * 房间局数
     */
    private String roomNum;

    /**
     * 剩余局数
     */
    private int shengyuRoomNum;

    /**
     * 最大倍数
     */
    private String multiple;

    /**
     * 抵注
     */
    private String blind;

    /**
     *
     */
    private String sharedIP;

    /**
     *
     */
    private String AA;

    /**
     *
     */
    private String method;




    public HomeInfoEntity(Player homeOwner, int personNum, Poker poker, String roomNum,
                          int shengyuRoomNum, String multiple, String blind,
                          String sharedIP, String AA, String method) {
        this.personNum = personNum;
        this.roomNum = roomNum;
        this.shengyuRoomNum = shengyuRoomNum;
        this.multiple = multiple;
        this.blind = blind;
        this.sharedIP = sharedIP;
        this.AA = AA;
        this.method = method;
        this.ownerId=homeOwner.getUid();
    }
    public HomeInfoEntity(HomeInfo homeInfo){
        this(homeInfo.getHomeOwner(),homeInfo.getPersonNum(),homeInfo.getPoker(),homeInfo.getRoomNum(),homeInfo.getShengyuRoomNum(),homeInfo.getMultiple()
        ,homeInfo.getBlind(),homeInfo.getSharedIP(),homeInfo.getAA(),homeInfo.getMethod());
    }
}
