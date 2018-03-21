package smile.tool;

import smile.config.ConfigurableEnvironment;
import smile.config.EnvironmentConverter;
import java.util.Properties;


public class ConfigPropertiesTools {

    /**
     *
     * @param args 启动配置信息
     * @param properties 应用配置信息
     * @return
     */
    private ConfigurableEnvironment prepareEnvironment(String[] args, Properties properties) {
        return new EnvironmentConverter(args,properties);
    }

}
