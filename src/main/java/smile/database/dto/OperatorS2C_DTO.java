package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

import java.util.List;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/29 下午9:08
 */
@Data
@Builder
public class OperatorS2C_DTO implements Datagram {
    /**
     * 用户操作状态
     */
    private String operationStatus;
    private List<String> pokers;
    /**
     * 座位号
     */
    private String preCharid;

    private String currentChairId;

    private String currentStatus;
}
