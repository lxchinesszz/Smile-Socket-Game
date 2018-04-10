package smile.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Package: smile.net
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/9 上午12:06
 */
public class MessageToByteEncoderss extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,String s, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(s.getBytes());
    }
}
