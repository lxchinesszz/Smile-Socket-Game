package smile.protocol;

/**
 * @Package: smile.protocol
 * @Description: 协议转换异常
 * @author: liuxin
 * @date: 2018/3/17 下午2:26
 */
public class ProtocolConvertException extends RuntimeException{
    public ProtocolConvertException(String message) {
    super(message);
}

    public ProtocolConvertException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
