package smile.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * @Package: smile.net
 * @Description: 拆包演示
 * @author: liuxin
 * @date: 2018/4/9 下午2:51
 */
public class SocketPageTest {
    public static void main(String[] args) throws Exception {

        ByteBuf byteBuf = Unpooled.buffer();
        String data = "你好你好你好";
        byte[] src_bytes = data.getBytes();
        int len = data.getBytes().length;
        //长度
//        byteBuf.writeInt(len+2);
        for (int i = 0; i < 4; i++) {
            byte[] bytes = (data + i).getBytes();
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
            chai(byteBuf);
        }
    }

    public static void chai1(ByteBuf byteBuf)throws Exception{
        int i = byteBuf.readableBytes();
        byte[]arr=new byte[i];
        byteBuf.readBytes(arr);
        System.out.println(new String(arr,"utf-8"));
    }


    public static void chai(ByteBuf byteBuf)throws Exception {
        int beginReader;
        byte[]arr;
        while (true) {
            // 标记包头开始的index
            byteBuf.markReaderIndex();
            // 获取包头开始的index
            beginReader = byteBuf.readerIndex();
            // 消息的长度
            int length = byteBuf.readInt();
            // 判断请求数据包数据是否到齐
            byteBuf.resetReaderIndex();
            // 收到数据总共长度
            int i1 = byteBuf.readableBytes();
            //判断数据包是否到齐
            if ( i1< length + 4) {
                // 还原读指针
                byteBuf.readerIndex(beginReader);
                return;
            }
            // 读取data数据
            byteBuf.readerIndex(beginReader+4);
            arr= new byte[length];
            byteBuf.readBytes(arr,0,length);
            System.out.println(new String(arr,"utf-8"));
            break;
        }
    }
}
