package smile.tool;

import smile.service.home.*;

/**
 * @Package: smile.tool
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/23 下午11:17
 */
public class GameHelper {

    private static HomeManager homeManager;

    static {
        //创建一个房间秘钥生成器
        HomeGenerator homeGenerator = new HomeGeneratorImpl();
        //创建一个房管,对房间进行管理
        homeManager = new HomeManagerImpl(homeGenerator);
    }

    private GameHelper() {
    }

    public static HomeManager homeManager() {
        return homeManager;
    }
}
