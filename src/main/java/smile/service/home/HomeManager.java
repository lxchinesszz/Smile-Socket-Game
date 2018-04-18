package smile.service.home;

/**
 * @Package: com.example.poker
 * @Description: 房管接口
 * @author: liuxin
 * @date: 2018/3/10 下午8:31
 */
public interface HomeManager {

    /**
     * 创建一个房间
     *
     * @return
     */
    Home createHome(HomeInfo homeType);

    /**
     * 创建一个房间
     *
     * @return
     */
    Home createHome(String hid,HomeInfo homeType);


    /**
     * 返回房间数量
     *
     * @return
     */
    Long homeNumber();

    /**
     * 根据房间号,获取房间
     * @param homeId
     * @return
     */
    Home getHome(String homeId);

    boolean updateHome(Home home);

    Home clearHome(String homeId);
}
