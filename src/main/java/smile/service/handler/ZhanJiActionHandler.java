package smile.service.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.database.domain.UserFighting;
import smile.database.dto.UserC2S_DTO;
import smile.database.dto.UserFightS2C_DTO;
import smile.database.dto.UserFightS2C_INNER_DTO;
import smile.database.mongo.MongoDao;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.SocketPackage;
import smile.tool.IOC;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

/**
 * @Package: smile.service.handler
 * @Description: 战绩计算
 * @date: 2018/4/16 下午11:52
 * @author: liuxin
 */
@SmileComponent
@Action
public class ZhanJiActionHandler extends AbstractActionHandler {
    @InsertBean
    private MongoDao mongoDao;
    @InsertBean
    private PlayerInfoNotify playerInfoNotify;

    @SubOperation(sub=20)
    public SocketPackage zhanji(SocketPackage socketPackage, Channel channel) {
        UserC2S_DTO datagram = (UserC2S_DTO) socketPackage.getDatagram();
        String uid = datagram.getUid();
        String query = String.format("{\"uid\":\"%s\"}", uid);
        List<UserFighting> all = mongoDao.findAll(query, UserFighting.class);
        if (all.size() == 0 || all == null) {
            socketPackage.setDatagram(new UserFightS2C_DTO("1", new ArrayList<UserFightS2C_INNER_DTO>()));
        } else {
            List<UserFightS2C_INNER_DTO> history_userFighting = Lists.newArrayList();
            Set<String> set = Sets.newHashSet();
            all.stream().forEach(userFighting -> set.add(userFighting.getHid()));
            set.forEach(new Consumer<String>() {
                @Override
                public void accept(String s) {
                    history_userFighting.add(jisuan(s, uid));
                }
            });
            socketPackage.setDatagram(new UserFightS2C_DTO("0", history_userFighting));
        }
        channel.writeAndFlush(socketPackage);
        return socketPackage;
    }


    public UserFightS2C_INNER_DTO jisuan(String hid, String uid1) {
        String query = String.format("{\"hid\":\"%s\",\"uid\":\"%s\"}", hid, uid1);
        List<UserFighting> all = mongoDao.findAll(query, UserFighting.class);
        String uid = all.get(0).getUid();
        Collections.sort(all, new Comparator<UserFighting>() {
            @Override
            public int compare(UserFighting o1, UserFighting o2) {
                long l = o2.getEndTime() - o1.getEndTime();
                return Integer.parseInt(l + "");
            }
        });

        long endTime = all.get(0).getEndTime();
        Collections.sort(all, new Comparator<UserFighting>() {
            @Override
            public int compare(UserFighting o1, UserFighting o2) {
                long l = o1.getEndTime() - o2.getEndTime();
                return Integer.parseInt(l + "");
            }
        });
        long startTime = all.get(0).getStartTime();
        BigDecimal gradeCount = BigDecimal.ZERO;
        for (UserFighting userFighting : all) {
            BigDecimal bigDecimal = BigDecimal.valueOf(Integer.parseInt(userFighting.getGrade()));
            gradeCount = gradeCount.add(bigDecimal);
        }
        return new UserFightS2C_INNER_DTO(uid, hid, startTime, endTime, gradeCount.toString());

    }
}
