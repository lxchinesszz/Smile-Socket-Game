package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: liuxin
 * @date: 2018/4/10 下午9:31
 */
@Data
public class ActiveS2C_DTO implements Datagram {
    private String code;
    private String cardNum;
    public ActiveS2C_DTO(boolean result,String cardnum){
        this.code=result==true?"0":"1";
        this.cardNum=cardnum;
    }
}
