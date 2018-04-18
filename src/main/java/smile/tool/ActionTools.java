package smile.tool;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smileframework.ioc.bean.context.BeanDefinition;
import org.smileframework.ioc.util.SmileContextTools;
import org.smileframework.tool.annotation.AnnotationMap;
import org.smileframework.tool.annotation.AnnotationTools;
import org.smileframework.tool.clazz.ClassTools;
import smile.global.annotation.Action;
import smile.global.annotation.SubOperation;
import smile.protocol.SocketPackage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Package: smile.tool
 * @Description: 将子协议号绑定
 * @author: mac
 * @date: 2018/4/16 下午4:06
 */
public class ActionTools {
    private static final Logger logger = LoggerFactory.getLogger(ActionTools.class);
    public static final Map<String, ActionModel> actionCache = Maps.newConcurrentMap();

//    public SocketPackage t(){
////        int sub = new SocketPackage().getProtocol().getSub();
//
//    }

    public static SocketPackage opera(byte sub, SocketPackage socketPackage, Channel channel) {
        ActionModel actionHandler = getActionHandler(sub);
        if (actionHandler == null) {
            throw new RuntimeException("为查询到副协议号:" + sub + ", 绑定处理器");
        }
        Object action = actionHandler.getBeanDefinition().getInstance();
        try {
            Method method = actionHandler.getMethod();
            method.setAccessible(true);
            method.invoke(action, socketPackage, channel);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return socketPackage;
    }

    public static ActionModel getActionHandler(byte sub) {
        return actionCache.get(String.valueOf(sub));
    }

    public static void action() {
        //TODO 拿到所有绑定的动作,进行处理
        Map<String, BeanDefinition> beanByAnnotation = SmileContextTools.getCurrentApplication().getBeanByAnnotation(Action.class);
        for (Map.Entry<String, BeanDefinition> ac : beanByAnnotation.entrySet()) {
            String beanName = ac.getKey();
            BeanDefinition beanDefinition = ac.getValue();
            bindSubAndMethod(beanDefinition);
        }
    }

    /**
     * 绑定副协议号和方法
     *
     * @param beanDefinition
     */
    private static void bindSubAndMethod(BeanDefinition beanDefinition) {
        Action annotation = beanDefinition.getClazz().getAnnotation(Action.class);
        if (annotation == null) {
            return;
        }
        Method[] methods = beanDefinition.getClazz().getDeclaredMethods();
        if (methods.length == 0) {
            return;
        }
        for (Method method : methods) {
            SubOperation subOperation = method.getAnnotation(SubOperation.class);
            if (subOperation == null) continue;
            AnnotationMap<String, Object> subOperaInfo = AnnotationTools.getAnnotationAttributeAsMap(subOperation);
            Object sub = subOperaInfo.get("sub");
            byte[] subs = (byte[]) sub;
            for (byte s : subs) {
                String subString = String.valueOf(s);
                actionCache.put(subString, new ActionModel(beanDefinition, method));
                logger.info("子协议号: " + subString + ",绑定类: " + ClassTools.getQualifiedName(beanDefinition.getClazz())+",绑定方法: "+method.getName());
            }
        }
    }

    public static class ActionModel {
        BeanDefinition beanDefinition;
        Method method;

        public ActionModel(BeanDefinition beanDefinition, Method method) {
            this.beanDefinition = beanDefinition;
            this.method = method;
        }

        public BeanDefinition getBeanDefinition() {
            return beanDefinition;
        }

        public void setBeanDefinition(BeanDefinition beanDefinition) {
            this.beanDefinition = beanDefinition;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        @Override
        public String toString() {
            return "ActionModel{" +
                    "beanDefinition=" + beanDefinition +
                    ", method=" + method +
                    '}';
        }
    }
}
