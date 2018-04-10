package smile.database.dto;

import lombok.Data;
import lombok.ToString;
import smile.protocol.Datagram;

/**
 * @Package: smile.database.dto
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/10 下午2:12
 */
@ToString
@Data
public class ChatC2S_DTO implements Datagram {
    private String hid; //房间号
    private String chairId;  //发送人ID
    private String faceId;   //表情ID
    private String voice;     //声音ID
    private String voiceUrl;  //下载语音的地址
}
