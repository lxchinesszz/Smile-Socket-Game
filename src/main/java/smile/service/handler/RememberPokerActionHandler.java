package smile.service.handler;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.json.JsonUtils;
import smile.config.ErrorEnum;
import smile.database.domain.PokerRecordEntity;
import smile.database.dto.UserC2S_DTO;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.SocketPackage;
import smile.protocol.impl.ResultDatagram;
import smile.protocol.impl.UserDatagram;
import smile.tool.DateTools;
import smile.tool.IOC;

import java.util.Map;

/**
 * @Package: smile.service.handler
 * @Description: 记牌器操作
 * @date: 2018/4/16 下午7:56
 * @author: liuxin
 */
@Action
@SmileComponent
public class RememberPokerActionHandler extends AbstractActionHandler{
    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;
    /**
     * 购买记牌器
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 23,model = UserC2S_DTO.class)
    SocketPackage buyJipaiqi(SocketPackage socketPackage, Channel channel) {
        UserC2S_DTO datagram = (UserC2S_DTO) socketPackage.getDatagram();
        String uid = datagram.getUid();
        UserDatagram byUid = mongoDao.findByUid(uid, UserDatagram.class);
        String cardNum = byUid.getCardNum();
        if (Integer.parseInt(cardNum) < 4) {
            ResultDatagram errorDatagram = new ResultDatagram(ErrorEnum.CARD_BUGOU);
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        String afterAdmingCard = String.valueOf(Integer.parseInt(cardNum) - 4);
        String userQuery = String.format("{\"uid\":\"%s\"}", uid);
        Map update = Maps.newConcurrentMap();
        update.put("cardNum", afterAdmingCard);
        boolean update1 = mongoDao.update(userQuery, update, UserDatagram.class);
        if (update1) {
            mongoDao.insert(new PokerRecordEntity(uid, DateTools.getDayEndTime().getTime()));
            ResultDatagram resultDatagram = new ResultDatagram();
            socketPackage.setDatagram(resultDatagram);
            channel.writeAndFlush(socketPackage);
        }
        return socketPackage;
    }

    /**
     * 验证记牌器是否过期
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 24,model = UserC2S_DTO.class)
    public SocketPackage checkJipaiqi(SocketPackage socketPackage, Channel channel) {
        UserC2S_DTO datagram = (UserC2S_DTO) socketPackage.getDatagram();
        String uid = datagram.getUid();
        String query = mongoDao.getFindJson("uid", uid);
        PokerRecordEntity one = mongoDao.findOne(query, PokerRecordEntity.class);
        //判断是否过期当前时间>记牌器结束时间就移除
        if (one == null) {
            ResultDatagram err = new ResultDatagram(ErrorEnum.JIPAIQI_FAIL);
            socketPackage.setDatagram(err);
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        if (one.getEndTime() < System.currentTimeMillis()) {
            mongoDao.del(query, mongoDao.getDocumetName(one.getClass()));
            ResultDatagram err = new ResultDatagram(ErrorEnum.JIPAIQI_FAIL);
            socketPackage.setDatagram(err);
        } else {
            ResultDatagram resultDatagram = new ResultDatagram();
            socketPackage.setDatagram(resultDatagram);
        }
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }
}
