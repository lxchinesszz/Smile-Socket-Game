package smile.service.handler;

import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.database.dto.ChatC2S_DTO;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.SocketPackage;
import smile.service.home.Home;
import smile.tool.GameHelper;
import smile.tool.IOC;

/**
 * @Package: smile.service.handler
 * @Description: 广播游戏中玩家的的消息
 * @date: 2018/4/16 下午11:55
 * @author: liuxin
 */
@Action
@SmileComponent
public class MessageNotityActionHandler extends AbstractActionHandler{
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;
    /**
     * 广播游戏中玩家的的消息
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 17)
    public SocketPackage charTM(SocketPackage socketPackage, Channel channel) {
        ChatC2S_DTO datagram = (ChatC2S_DTO) socketPackage.getDatagram();
        String hid = datagram.getHid();
        Home home = GameHelper.homeManager().getHome(hid);
        playerInfoNotify.operator(home, socketPackage);
        return socketPackage;
    }

}
