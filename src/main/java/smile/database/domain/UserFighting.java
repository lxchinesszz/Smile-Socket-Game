package smile.database.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.time.DateUtils;
import org.smileframework.tool.date.DateFormatTools;
import smile.config.Table;
import smile.tool.DateTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
public class UserFighting {
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
    private long startTime;
    /**
     * 结束时间
     */
    private long endTime;
    /**
     * 分数
     */
    private String grade;

    public UserFighting(String uid, String hid, long startTime, long endTime, String grade) {
        this.uid = uid;
        this.hid = hid;
        SimpleDateFormat simpleDateFormat = null;
        simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
        this.startTime = startTime;
        this.endTime = endTime ;
        this.grade = grade;
    }

}
