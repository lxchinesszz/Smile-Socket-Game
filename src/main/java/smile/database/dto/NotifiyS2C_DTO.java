package smile.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import smile.database.domain.NotifiyEntity;
import smile.protocol.Datagram;

import java.util.List;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: mac
 * @date: 2018/4/15 下午1:37
 */
@Data
@AllArgsConstructor
public class NotifiyS2C_DTO implements Datagram{
    private String code;
    private List<NotifiyEntity> notifiyEntities;
}
