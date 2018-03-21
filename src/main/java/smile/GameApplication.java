package smile;

import smile.config.ConfigurableEnvironment;
import smile.config.EnvironmentConverter;
import smile.net.GameServer;
import java.util.Map;
import java.util.Properties;

/**
 * @Package: smile
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/21 上午11:05
 */
public class GameApplication {
    public static void main(String[] args) throws Exception {
        /**
         * 应用配置信息
         */
        Properties properties=new Properties();
        /**
         * 系统配置信息
         */
        ConfigurableEnvironment configurableEnvironment = new EnvironmentConverter(args, properties);
        Map<String, String> optionInfo = configurableEnvironment.getOptionInfo();
        GameServer.start(10101);
    }
}
