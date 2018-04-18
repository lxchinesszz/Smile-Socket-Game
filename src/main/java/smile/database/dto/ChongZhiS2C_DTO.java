package smile.database.dto;

import lombok.Builder;
import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: mac
 * @date: 2018/4/15 下午2:30
 */
@Data
@Builder
public class ChongZhiS2C_DTO implements Datagram {
    private String code;
    private String cardNum;

    public ChongZhiS2C_DTO(String cardNum) {
        this("0",cardNum);
    }

    public ChongZhiS2C_DTO(String code, String cardNum) {
        this.code = code;
        this.cardNum = cardNum;
    }
}
