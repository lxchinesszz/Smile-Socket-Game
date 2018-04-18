package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: mac
 * @date: 2018/4/14 下午2:07
 */
@Data
public class UserC2S_DTO implements Datagram {
    private String uid;
}
