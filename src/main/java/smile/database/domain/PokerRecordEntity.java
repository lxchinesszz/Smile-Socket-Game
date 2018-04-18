package smile.database.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import smile.config.Table;

/**
 * @Package: smile.database.domain
 * @Description: ${todo}
 * @author: mac
 * @date: 2018/4/15 下午3:37
 */
@Data
@AllArgsConstructor
@Table(name = "ddz_jipaiqi")
public class PokerRecordEntity {
    private String uid;
    private long endTime;
}
