package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/3 下午2:01
 */
@Data
public class CheckPokerS2C_DTO implements Datagram{
    private String isAllow;

    public CheckPokerS2C_DTO(boolean isAllow) {
       if (isAllow){
           this.isAllow="1";
       }else {
           this.isAllow="0";
       }
    }
}
