package smile.config;

/**
 * @Package: org.smileframework.ioc.bean.context
 * @Description: 全局环境信息
 *  developer 可以根据该类,获取到应用的全部参数
 * @author: liuxin
 * @date: 2017/12/7 下午12:49
 */
public interface Environment {
    /**
     * 获取应用配置信息
     * @param key
     * @return
     */
    String getProperty(String key);


    String getProperty(String key, String defultValue);
}
