package smile;

import org.bson.Document;
import org.smileframework.ioc.bean.annotation.SmileBootApplication;
import org.smileframework.ioc.bean.context.*;
import org.smileframework.ioc.util.SmileContextTools;
import smile.database.domain.NotifiyEntity;
import smile.database.dto.NotifiyS2C_DTO;
import smile.database.mongo.MongoDao;
import smile.net.GameServer;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.service.handler.BreakActionHandler;
import smile.tool.ActionTools;
import smile.tool.IOC;

import java.util.*;

/**
 * @Package: smile
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/21 上午11:05
 */
@SmileBootApplication
public class GameApplication {


    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext configurableApplicationContext = SmileApplication.run(GameApplication.class, args);
        IOC.setConfigurableApplicationContext(configurableApplicationContext);
        GameServer.start(10101);
        ActionTools.action();
        BreakActionHandler bean = configurableApplicationContext.getBean(BreakActionHandler.class);
//        MongoDao mongoDao = configurableApplicationContext.getBean(MongoDao.class);
//        List<UserEntity> all = mongoDao.findAll(UserEntity.class);
//        for (UserEntity userEntity:all){
//            System.out.println(userEntity);
//        }
//
//        UserDatagram one = mongoDao.findOne(query, UserDatagram.class);
//        System.out.println(one);
//
//        UserEntity byUid = mongoDao.findByUid("125997", UserEntity.class);
//        System.out.println(byUid);
        ConfigApplicationContext currentApplication = (ConfigApplicationContext) SmileContextTools.getCurrentApplication();
        MongoDao mongoDao = currentApplication.getBean(MongoDao.class);
        Document query2 = new Document("endTime", new Document("$gt", System.currentTimeMillis()));
        List<NotifiyEntity> all = mongoDao.findAll(query2.toJson(),NotifiyEntity.class);
        SocketPackage socketPackage = new SocketPackage(new Protocol(2, 21));
        if (all!=null&all.size()>0){
            socketPackage.setDatagram(new NotifiyS2C_DTO("0",all));
        }else {
            socketPackage.setDatagram(new NotifiyS2C_DTO("-1",all));
        }
    }
}
