package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

import java.util.List;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/29 下午11:04
 */
@Data
@Builder
public class DizhuS2C_DTO implements Datagram {
    private String chaird;
    private List<String> pokers;
}
