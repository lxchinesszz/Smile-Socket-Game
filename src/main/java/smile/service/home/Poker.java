package smile.service.home;

import smile.service.poker.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: com.example.poker
 * @Description: 扑克牌
 * @author: liuxin
 * @date: 2018/3/10 下午9:28
 */
public interface Poker {


    //获取整副扑克牌
    ArrayList<Card> getPoker();

    //重洗扑克牌
    void pokerShuffle();

    /**
     * 发牌逻辑
     *
     * @param players
     */
     void deal(List<Player> players);

    /**
     * 获取地主牌
     * @return
     */
     ArrayList<Card> getMainPoker();
}
