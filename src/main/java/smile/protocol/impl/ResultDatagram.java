package smile.protocol.impl;

import lombok.Data;
import smile.config.ErrorEnum;
import smile.protocol.Datagram;

/**
 * @Package: smile.protocol.impl
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/23 下午11:35
 */
@Data
public class ResultDatagram implements Datagram {
    private int code;
    private String message;

    public ResultDatagram(ErrorEnum errorEnum){
        this.code=errorEnum.getCode();
    }

//    public ResultDatagram(int code, String message) {
//        this.code = code;
//        this.message = message;
//    }

    public ResultDatagram() {
        this.code=0;
    }

//    public ResultDatagram() {
//        this("处理成功");
//    }


}
