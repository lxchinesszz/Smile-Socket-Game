package smile.protocol;

import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.ioc.util.SmileContextTools;
import smile.service.handler.Action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Package: smile.protocol
 * @Description: 协议管理器, 根据协议类型, 找到对应的处理多级
 * @author: liuxin
 * @date: 2018/3/30 下午4:31
 */
@SmileComponent
public class ActionManager {

    public void action(SocketPackage socketPackage){
        int sub = socketPackage.getProtocol().getSub();

        //TODO 拿到所有绑定的动作,进行处理

        SmileContextTools.getCurrentApplication().getBean(Action.class);
    }
}
