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
    private volatile ArrayList<Card> poker = new ArrayList();
    private volatile ArrayList<Card> mainPoker = new ArrayList<>();

    public CardPoker(String personNum) {
        this(Integer.parseInt(personNum));
    }

    public CardPoker(int personNum) {
        int cardNum = 55;
        if (personNum == 4) {
            cardNum = 53;
        }
        /**
         * 构建54张牌
         */
        for (int i = 1; i < cardNum; i++) {
            poker.add(new Card(i));
        }
    }

    //获取整副扑克牌
    @Override
    public ArrayList<Card> getPoker() {
        return this.poker;
    }

    /**
     * 洗牌10次
     * 重洗扑克牌
     */
    @Override
    public void pokerShuffle() {
        for (int i = 0; i < 10; i++) {
            Collections.shuffle(poker);
        }
    }

    public void setPoker(ArrayList<Card> poker) {
        this.poker = poker;
    }

    public ArrayList<Card> getMainPoker() {
        return mainPoker;
    }

    public void setMainPoker(ArrayList<Card> mainPoker) {
        this.mainPoker = mainPoker;
    }

    /**
     * 发牌逻辑
     *
     * @param players
     */
    public void deal(List<Player> players) {
        int zhuPokers =4;
        if (players.size()==3){
            zhuPokers=3;
        }
        for (int i = 0; i <zhuPokers; i++) {
            this.mainPoker.add(getPoker().remove(i));
        }
        for (int i = 0; i < getPoker().size(); i++) {
            if (i % 4 == 0) {
                players.get(0).getPoker().add(getPoker().get(i));
            } else if (i % 4 == 1) {
                players.get(1).getPoker().add(getPoker().get(i));
            } else if (i % 4 == 2) {
                players.get(2).getPoker().add(getPoker().get(i));
            } else if (i % 4 == 3) {
                players.get(3).getPoker().add(getPoker().get(i));
            }
        }
    }
}
