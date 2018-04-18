package smile.database.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import smile.config.Table;
import smile.database.domain.UserFighting;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Package: smile.database.domain
 * @Description: 游戏战绩
 * @author: mac
 * @date: 2018/4/14 下午1:44
 */
@Data
@NoArgsConstructor
@ToString
@Table(name = "ddz_user_fighting")
public class UserFightS2C_INNER_DTO {
    /**
     * 用户id
     */
    private String uid;
    /**
     * 房间号
     */
    private String hid;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 分数
     */
    private String grade;


    public UserFightS2C_INNER_DTO(UserFighting uf){
        this(uf.getUid(),uf.getHid(),uf.getStartTime(),uf.getEndTime(),uf.getGrade());
    }
    public UserFightS2C_INNER_DTO(String uid, String hid, long startTime, long endTime, String grade) {
        this.uid = uid;
        this.hid = hid;
        SimpleDateFormat simpleDateFormat = null;
        simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
        this.startTime = simpleDateFormat.format(new Date(startTime));
        this.endTime = simpleDateFormat.format(new Date(endTime));
        this.grade = grade;
    }

}
