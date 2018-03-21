package smile.service.home;

import smile.service.poker.Card;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @Package: com.example.poker
 * @Description: //斗地主玩家
 * @author: liuxin
 * @date: 2018/3/10 下午8:28
 */
public class Player{
    private String name;//玩家姓名
    private ArrayList<Card> poker;//玩家手中的牌


    public Player(String name) {
        super();
        this.name = name;
        poker = new ArrayList();
    }

    public ArrayList<Card> getPoker() {
        Collections.sort(poker);
        return poker;
    }


    public void setPoker(ArrayList<Card> poker) {
        this.poker = poker;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Player other = (Player) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }



}
