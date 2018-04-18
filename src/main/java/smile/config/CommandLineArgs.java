package smile.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;

/**
 * @Package: org.smileframework.ioc.bean.context
 * @Description: 命令行参数
 * @author: liuxin
 * @date: 2017/12/7 下午6:37
 */
public class CommandLineArgs {
    private final Map<String, String> optionArgs = Maps.newConcurrentMap();
    private final List<String> nonOptionArgs = Lists.newArrayList();

    public CommandLineArgs() {
    }

    public void addOptionArg(String optionName, String optionValue) {
        if (!this.optionArgs.containsKey(optionName)) {
            this.optionArgs.put(optionName, optionValue);
        }

        if (optionValue == null) {
            this.optionArgs.put(optionName, "");
        }

    }

    public Set<String> getOptionNames() {
        return Collections.unmodifiableSet(this.optionArgs.keySet());
    }

    public boolean containsOption(String optionName) {
        return this.optionArgs.containsKey(optionName);
    }

    public String getOptionValue(String optionName) {
        return this.optionArgs.get(optionName);
    }

    public void addNonOptionArg(String value) {
        this.nonOptionArgs.add(value);
    }

    public List<String> getNonOptionArgs() {
        return Collections.unmodifiableList(this.nonOptionArgs);
    }
}
