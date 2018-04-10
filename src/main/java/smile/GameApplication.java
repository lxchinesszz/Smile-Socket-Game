package smile;

import org.smileframework.ioc.bean.annotation.SmileBootApplication;
import org.smileframework.ioc.bean.context.*;
import org.smileframework.ioc.util.SmileContextTools;
import smile.database.domain.UserEntity;
import smile.database.mongo.MongoDao;
import smile.global.annotation.ActionMapping;
import smile.net.GameServer;
import smile.service.handler.Action;
import smile.tool.IOC;

import java.util.Map;

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

        Map<String, BeanDefinition> beanByAnnotation = configurableApplicationContext.getBeanByAnnotation(ActionMapping.class);
    }
}
