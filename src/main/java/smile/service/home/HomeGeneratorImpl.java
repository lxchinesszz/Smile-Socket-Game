package smile.service.home;

import smile.tool.StdRandom;

public class HomeGeneratorImpl implements HomeGenerator {
    @Override
    public Home createHome(HomeInfo homeType) {
        return new Home(homeType,homeKey());
    }

    @Override
    public Home createHome(String uid,HomeInfo homeType) {
        return new Home(homeType,Integer.parseInt(uid));
    }

    @Override
    public Integer homeKey() {
        return StdRandom.generate6BitInt();
    }


}
