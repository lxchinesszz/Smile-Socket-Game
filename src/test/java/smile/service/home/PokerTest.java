package smile.service.home;

import junit.framework.TestCase;
import smile.service.poker.Card;
import smile.service.poker.CardType;
import smile.service.poker.GameRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @Package: smile.service.home
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/2 下午11:04
 */
public class PokerTest extends TestCase {
    /**
     * 测试移除牌
     *
     * @param args
     */
    public static void main(String[] args) {
//16,17,44,45,20
        CardType myCardType = GameRule.getCardType(Arrays.asList(new Card(16),new Card(17),new Card(44),new Card(45),new Card(20)));

        System.err.println(myCardType);
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(2));
        cards.add(new Card(3));
        cards.add(new Card(4));
        cards.add(new Card(5));
        cards.add(new Card(7));

        Card card0 = new Card(2);
        System.out.println(cards);
        cards.removeIf(new Predicate<Card>() {
            @Override
            public boolean test(Card card) {
                if (card.id == card0.id
                        & card.bigType.equals(card0.bigType)
                        & card.grade == card0.grade
                        & card.smallType.equals(card0.smallType)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        System.out.println(cards);
    }
}

