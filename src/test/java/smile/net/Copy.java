package smile.net;

/**
 * @Package: smile.net
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/20 下午10:55
 */
public class Copy {
    public static void main(String[] args) {
        byte[] b1 = new byte[]{1, 2};
        byte[] b2 = new byte[]{3, 4};

        byte[] body = new byte[b1.length + b2.length];
        System.arraycopy(b1,0,body,0,b1.length);
        System.arraycopy(b2,0,body,b2.length,b1.length);

    }
}
