package smile.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import smile.database.domain.UserFighting;
import smile.protocol.Datagram;

import java.util.List;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: mac
 * @date: 2018/4/14 下午2:10
 */
@Data
@Builder
public class UserFightS2C_DTO implements Datagram {
    private String code;
    List<UserFightS2C_INNER_DTO> userFightings;

    public UserFightS2C_DTO(String code, List<UserFightS2C_INNER_DTO> userFightings) {
        this.code = code;
        this.userFightings = userFightings;
    }
}
