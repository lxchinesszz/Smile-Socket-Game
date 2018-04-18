package smile.database.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.smileframework.tool.date.DateFormatTools;
import smile.config.Table;
import smile.protocol.Datagram;
import smile.tool.DateTools;

/**
 * @Package: smile.database.domain
 * @Description: 公告
 * @author: mac
 * @date: 2018/4/15 上午11:29
 */
@Data
@NoArgsConstructor
@Table(name = "notify_message")
public class NotifiyEntity {
    private String context;
    private long startTime;
    private long endTime;

    /**
     *
     * @param context
     * @param startTime
     * @param endTime
     */
    public NotifiyEntity(String context,String startTime,String endTime) {
        this.context = context;
        this.startTime=DateTools.format(startTime).getTime();
        this.endTime=DateTools.format(endTime).getTime();
    }
}
