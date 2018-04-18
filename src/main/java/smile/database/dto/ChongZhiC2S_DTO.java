package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: mac
 * @date: 2018/4/15 下午2:28
 */
@Data
public class ChongZhiC2S_DTO implements Datagram{
    private String uid;
    private String cardNum;
    private String rechargeId;
}
