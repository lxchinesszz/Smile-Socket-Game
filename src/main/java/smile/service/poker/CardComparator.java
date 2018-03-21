package smile.service.poker;

import java.util.Arrays;
import java.util.Comparator;


public class CardComparator implements Comparator<Card> {

    @Override
    public int compare(Card card1, Card card) {
        int result = -1;

        int grade1 = card1.grade;
        int grade2 = card.grade;

        if (grade1 > grade2) {
            result = 1;
        } else if (grade1 < grade2) {
            result = -1;
        } else {
            // 等级相同的情况，比如都是9
            Card.CardBigType bigType1 = card1.bigType;
            Card.CardBigType bigType2 = card.bigType;
            // 从左到右，方块、梅花、红桃、黑桃
            if (bigType1.equals(Card.CardBigType.HEI_TAO)) {
                result = 1;
            } else if (bigType1.equals(Card.CardBigType.HONG_TAO)) {
                if (bigType2.equals(Card.CardBigType.MEI_HUA)
                        || bigType2.equals(Card.CardBigType.FANG_KUAI)) {
                    result = 1;
                }
            } else if (bigType1.equals(Card.CardBigType.MEI_HUA)) {
                if (bigType2.equals(Card.CardBigType.FANG_KUAI)) {
                    result = 1;
                }
            }
            // 2张牌的等级不可能完全相同,程序内部采用这种设计
            else {
                result = -1;
            }
        }

        return result;
    }


    public static void main(String[] args) {
        Card A=new Card(1);//A
        Card ER=new Card(2);//2
        Card K=new Card(13);
        CardComparator cardComparator=new CardComparator();
        int compare = cardComparator.compare(A, K);
        System.out.println(compare);

        boolean duiZi = GameRule.isDuiZi(Arrays.asList(A, new Card(1)));
        System.out.println(duiZi);
    }
}

