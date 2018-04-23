package smile;


import com.google.common.collect.Lists;
import smile.service.poker.Card;
import smile.service.poker.CardType;
import smile.service.poker.CardUtil;
import smile.service.poker.GameRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Package: smile
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/27 下午2:12
 */
public class Main {

    public static void main(String[] args) {

        List<Card> poker = Lists.newArrayList();
        System.out.println(new Card(2));
        poker.add(new Card(2));
        poker.add(new Card(2));
        System.out.println(new Card(45));
        poker.add(new Card(45));
        poker.add(new Card(45));
        System.out.println(new Card(7));
        poker.add(new Card(7));
        System.out.println(new Card(10));
        poker.add(new Card(10));
        System.out.println(new Card(9));
        poker.add(new Card(9));
        System.out.println(new Card(34));
        poker.add(new Card(34));
        poker.add(new Card(34));
        poker.add(new Card(34));
        poker.add(new Card(34));
        CardUtil.sortCards(poker);
        System.out.println(poker);
        //模拟三代一和三不带和单张
        List<Card> overcomePrevCards = GameRule.getOvercomePrevCards(poker, Arrays.asList(new Card(45), new Card(45), new Card(45)), CardType.SAN_BU_DAI);
        System.out.println(overcomePrevCards);

        //
        List<Card> noni1 = GameRule.getOvercomePrevCards(poker, Arrays.asList(new Card(45), new Card(45), new Card(45),new Card(25)), CardType.SAN_DAI_YI);
        System.out.println(noni1);

        List<Card> noni2 = GameRule.getOvercomePrevCards(poker, null, null);
        System.out.println(noni2);



    }

}
