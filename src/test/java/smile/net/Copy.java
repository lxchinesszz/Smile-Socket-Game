package smile.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

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

        byte[] bytes = "-1".getBytes();

        ByteBuf directBuf = Unpooled.copiedBuffer("-1".getBytes());
//        String string = directBuf.toString(CharsetUtil.UTF_8);
//        System.out.println(string);
        byte[] arr = null;
        if (!directBuf.hasArray()) {
            int len = directBuf.readableBytes();
            arr = new byte[len];
            directBuf.getBytes(0, arr);
        }
        System.out.println(arr);


        ByteBuf b= Unpooled.buffer();
        b.writeByte(18);
        b.writeByte(17);
        b.writeInt(123);
        byte[]bb=new byte[b.readableBytes()];
        b.getBytes(0,bb);
        System.out.println(bb);


    }
}
