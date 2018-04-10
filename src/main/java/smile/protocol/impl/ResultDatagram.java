package smile.protocol.impl;

import lombok.Data;
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

    public ResultDatagram(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultDatagram(String message) {
        this(0, message);
    }

    public ResultDatagram() {
        this("处理成功");
    }


}
