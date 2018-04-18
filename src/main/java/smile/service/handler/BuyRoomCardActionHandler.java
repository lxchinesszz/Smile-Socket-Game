package smile.service.handler;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.tool.json.JsonUtils;
import smile.database.domain.UserEntity;
import smile.database.dto.ActiveC2S_DTO;
import smile.database.dto.ActiveS2C_DTO;
import smile.database.dto.ChongZhiC2S_DTO;
import smile.database.dto.ChongZhiS2C_DTO;
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
 * @Description: 充值方法
 * @date: 2018/4/16 下午11:49
 * @author: liuxin
 */
@SmileComponent
@Action
public class BuyRoomCardActionHandler extends AbstractActionHandler{
    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;

    /**
     * 提供给供应商充值接口
     * 1. 查询是否是供应商
     * 2. 查询供应商房卡是否大于将要减去的方房卡
     *
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub = 22)
    public SocketPackage chongzhi(SocketPackage socketPackage, Channel channel) {
        ChongZhiC2S_DTO datagram = (ChongZhiC2S_DTO) socketPackage.getDatagram();
        String uid = datagram.getUid();
        String rechargeId = datagram.getRechargeId();
        String cardNum = datagram.getCardNum();
        //1. 查询是否是供应商
        UserDatagram admin = mongoDao.findByUid(uid, UserDatagram.class);
        if (!"1".equalsIgnoreCase(admin.getIsAdmin())) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前用户非供应商");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        //2.
        String beforeCard = admin.getCardNum();
        if (Integer.parseInt(beforeCard) < Integer.parseInt(cardNum)) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "房卡数量不足,不能充值(房卡数:" + admin.getCardNum() + ")");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        String afterAdmingCard = String.valueOf(Integer.parseInt(beforeCard) - Integer.parseInt(cardNum));
        String admingQuery = String.format("{\"uid\":\"%s\"}", uid);
        Map admingUpdate = Maps.newConcurrentMap();
        admingUpdate.put("cardNum", afterAdmingCard);
        boolean admingUpdateFlag = mongoDao.update(admingQuery, admingUpdate, UserEntity.class);
        if (!admingUpdateFlag) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "房卡扣除失败,请稍后再试");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        UserDatagram rechargeUser = mongoDao.findByUid(rechargeId, UserDatagram.class);
        String after_card_num = String.valueOf(Integer.parseInt(rechargeUser.getCardNum()) + Integer.parseInt(cardNum));
        String query = String.format("{\"uid\":\"%s\"}", rechargeId);
        Map update = Maps.newConcurrentMap();
        update.put("cardNum", after_card_num);
        boolean updateFlag = mongoDao.update(query, update, UserEntity.class);
        if (updateFlag) {
            socketPackage.setDatagram(new ChongZhiS2C_DTO(afterAdmingCard));
            channel.writeAndFlush(socketPackage);
        }
        return socketPackage;
    }


    /**
     * 微信分享送房卡
     * @param socketPackage
     * @param channel
     * @return
     */
    @SubOperation(sub=18)
    public SocketPackage active(SocketPackage socketPackage, Channel channel) {
        ActiveC2S_DTO datagram = (ActiveC2S_DTO) socketPackage.getDatagram();
        String active = datagram.getActive();
        String uid = datagram.getUid();
        boolean result = false;
        UserEntity userEntity = mongoDao.findByUid(uid, UserEntity.class);
        if (userEntity == null) {
            ResultDatagram errorDatagram = new ResultDatagram(-1, "当前uid不存在");
            socketPackage.getProtocol().setSub((byte) 99);
            socketPackage.setDatagram(errorDatagram);
            System.err.println(JsonUtils.toJson(errorDatagram));
            channel.writeAndFlush(socketPackage);
            return socketPackage;
        }
        if (userEntity.getTimestamp() < DateTools.getDayStartTime().getTime()) {
            userEntity.setCardNum((Integer.parseInt(userEntity.getCardNum()) + 1) + "");
            String query = String.format("{\"uid\":\"%s\"}", uid);
            mongoDao.del(query, "ddz_user");
            userEntity.setTimestamp(System.currentTimeMillis());
            mongoDao.insert(userEntity);
            result = true;
        }
        channel.writeAndFlush(new ActiveS2C_DTO(result, (Integer.parseInt(userEntity.getCardNum()) + 1) + ""));
        return socketPackage;
    }
}
