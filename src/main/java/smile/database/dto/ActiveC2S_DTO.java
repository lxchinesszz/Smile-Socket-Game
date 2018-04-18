package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/10 下午9:31
 */
@Data
public class ActiveC2S_DTO implements Datagram {
    private String active;
    private String uid;
}
