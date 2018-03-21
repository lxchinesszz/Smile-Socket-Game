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
     * 返回房间数量
     *
     * @return
     */
    Long homeNumber();
}
