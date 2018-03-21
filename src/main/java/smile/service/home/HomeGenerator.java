package smile.service.home;

/**
 * @Package: com.example.poker
 * @Description: 房间生成规则
 * @author: liuxin
 * @date: 2018/3/10 下午8:45
 */
public interface HomeGenerator {


    /**
     * 房间钥匙或房间号
     * @return
     */
    Integer homeKey();


    /**
     * 房间信息:
     * 包括
     * 房主
     * 最大人数
     * 牌局数量
     * @param homeInfo
     * @return
     */
    Home createHome(HomeInfo homeInfo);


}
