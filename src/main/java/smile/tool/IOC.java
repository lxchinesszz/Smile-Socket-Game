package smile.tool;

import org.smileframework.ioc.bean.context.ConfigurableApplicationContext;

/**
 * @Package: smile.tool
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/23 下午9:31
 */
public class IOC {

    private static ConfigurableApplicationContext configurableApplicationContext;

    private IOC() {

    }

    public static void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext){
        configurableApplicationContext=applicationContext;
    }

    public static ConfigurableApplicationContext get(){
        return configurableApplicationContext;
    }
}
