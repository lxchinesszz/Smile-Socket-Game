package smile.protocol;

import org.smileframework.ioc.bean.annotation.SmileComponent;
import org.smileframework.ioc.bean.context.BeanDefinition;
import org.smileframework.ioc.util.SmileContextTools;
import smile.global.annotation.Action;

import java.util.Map;

/**
 * @Package: smile.protocol
 * @Description: 协议管理器, 根据协议类型, 找到对应的处理多级
 * @author: liuxin
 * @date: 2018/3/30 下午4:31
 */
@SmileComponent
public class ActionManager {

    public void action(SocketPackage socketPackage){
        //拿到副协议号
        int sub = socketPackage.getProtocol().getSub();
        //TODO 通过协议号找到处理类
        //TODO 拿到所有绑定的动作,进行处理
        Map<String, BeanDefinition> beanByAnnotation = SmileContextTools.getCurrentApplication().getBeanByAnnotation(Action.class);
    }
}
