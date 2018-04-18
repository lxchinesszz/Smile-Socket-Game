package smile.database.dto;

import lombok.ToString;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: mac
 * @date: 2018/4/16 下午3:18
 */
@ToString
public class PokerCount {
    private String uid;
    private String charId;
    private String pokerCount;

    public PokerCount(String uid,String charid, String pokerCount) {
        this.uid=uid;
        this.charId = charid;
        this.pokerCount = pokerCount;
    }
}
