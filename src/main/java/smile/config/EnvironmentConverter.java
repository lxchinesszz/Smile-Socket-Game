package smile.config;

import smile.tool.SmileCommandLineArgsParser;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Copyright (c) 2015 The Smile-Boot Project
 * <p>
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @Package: org.smileframework.ioc.bean.context
 * @Description: 环境信息
 * @author: liuxin
 * @date: 2017/12/7 下午12:52
 */
public class EnvironmentConverter implements ConfigurableEnvironment {
    /**
     * Java 启动配置信息
     */
    private CommandLineArgs commandLineArgs;

    private Map<String, String> optionAsMap;
    /**
     * 请用上下文配置
     */
    private Properties applicationEnvironment;
    /**
     * 系统启动环境
     */
    private Properties systemProperties;


    public EnvironmentConverter(String[] args, Properties properties) {
        this.commandLineArgs = new SmileCommandLineArgsParser().parse(args);
        this.applicationEnvironment = properties;
    }

    /**
     * 获取应用配置信息
     * @param key
     * @return
     */
    @Override
    public String getProperty(String key) {
        return applicationEnvironment.getProperty(key);
    }

    /**
     * 获取应用上下文配置信息
     * @param key
     * @param defaultValue
     * @return
     */
    @Override
    public String getProperty(String key, String defaultValue) {
        return applicationEnvironment.getProperty(key, defaultValue);
    }

    /**
     * Java启动配置信息
     * @return
     */
    public Map<String, String> getOptionInfo() {
        if (optionAsMap == null) {
            optionAsMap = new ConcurrentHashMap<>();
            Set<String> optionNames = commandLineArgs.getOptionNames();
            optionNames.forEach(option -> {
                String optionValue = commandLineArgs.getOptionValue(option);
                optionAsMap.put(option, optionValue);
            });
        }
        return optionAsMap;
    }

    @Override
    public Properties getEnvironment() {
        return this.applicationEnvironment;
    }

    @Override
    public Properties getSystemProperties() {
        if (systemProperties == null) {
            this.systemProperties = System.getProperties();
        }
        return systemProperties;
    }

    @Override
    public void merge(ConfigurableEnvironment var1) {

    }
}
