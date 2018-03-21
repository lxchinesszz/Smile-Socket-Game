package smile.service.poker;

import smile.service.home.Player;
import smile.service.home.Poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Package: com.example.poker
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/11 下午8:10
 */
public class CardPoker implements Poker {
    private ArrayList<Card> poker = new ArrayList();

    public CardPoker() {
        /**
         * 构建54张牌
         */
        for (int i = 1; i < 55; i++) {
            poker.add(new Card(i));
        }
    }


    //获取整副扑克牌
    @Override
    public ArrayList<Card> getPoker() {
        return poker;
    }

    /**
     *     洗牌10次
     *     重洗扑克牌
     */
    @Override
    public void pokerShuffle() {
        for (int i = 0; i < 10; i++) {
            Collections.shuffle(poker);
        }
    }

    /**
     * 发牌逻辑
     *
     * @param players
     */
    public void deal(List<Player> players) {
        for (int i = 0; i < getPoker().size(); i++) {
            if (i % 3 == 0) {
                players.get(0).getPoker().add(getPoker().get(i));
            } else if (i % 3 == 1) {
                players.get(1).getPoker().add(getPoker().get(i));
            } else if (i % 3 == 2) {
                players.get(2).getPoker().add(getPoker().get(i));
            }
        }
    }
}
