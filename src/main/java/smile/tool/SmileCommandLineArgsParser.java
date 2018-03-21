package smile.tool;

import smile.config.CommandLineArgs;

/**
 * @Package: org.smileframework.ioc.util
 * @Description: 命令行输出参数解析类
 * 可以将
 * java Main --server.active.profile=dev
 * 分解成
 * @author: liuxin
 * @date: 2017/12/7 下午6:34
 */
public class SmileCommandLineArgsParser {

    /**
     * -- 开头的会保存起来并根据= 拆分
     * 没有--开头的
     * @param args
     * @return
     */
    public CommandLineArgs parse(String... args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        String[] var3 = args;
        int var4 = args.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String arg = var3[var5];
            if (arg.startsWith("--")) {
                String optionText = arg.substring(2, arg.length());
                String optionValue = null;
                String optionName;
                if (optionText.contains("=")) {
                    optionName = optionText.substring(0, optionText.indexOf("="));
                    optionValue = optionText.substring(optionText.indexOf("=") + 1, optionText.length());
                } else {
                    optionName = optionText;
                }

                if (optionName.isEmpty() || optionValue != null && optionValue.isEmpty()) {
                    throw new IllegalArgumentException("Invalid argument syntax: " + arg);
                }

                commandLineArgs.addOptionArg(optionName, optionValue);
            } else {
                commandLineArgs.addNonOptionArg(arg);
            }
        }

        return commandLineArgs;
    }


    public static void main(String[] args) {
        String x = "server.port=23";
        String x1 = "--server.rpc=";
        String x2 = "--server.rpc=false";
        CommandLineArgs parse = new SmileCommandLineArgsParser().parse(x, x1, x2);
        System.out.println(parse);
        System.out.println(parse.getOptionValue("server.rpc"));

        parse.getNonOptionArgs().stream().map(str -> {
            return str.toUpperCase();
        }).forEach(str -> {
            System.out.println(str);
        });
    }
}
