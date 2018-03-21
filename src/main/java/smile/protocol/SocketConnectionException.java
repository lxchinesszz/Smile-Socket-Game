package smile.protocol;

/**
 * @Package: com.netty
 * @Description:
 * @author: liuxin
 * @date: 2018/3/16 下午6:01
 */
public class SocketConnectionException extends RuntimeException {
    public SocketConnectionException(String message) {
        super(message);
    }

    public SocketConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
