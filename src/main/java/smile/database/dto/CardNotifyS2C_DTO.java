package smile.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/10 下午10:04
 */
@Data
@AllArgsConstructor
public class CardNotifyS2C_DTO implements Datagram
{
    private String uid;
    private String cardNum;
}
