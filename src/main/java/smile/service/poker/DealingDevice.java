package smile.service.poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * @Package: com.example.poker.poker2
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/11 下午8:40
 */
public class DealingDevice {
    // 我的牌
    private ArrayList<Card> myCards = new ArrayList<Card>(17);

    // 左边玩家的牌
    private ArrayList<Card> leftCards = new ArrayList<Card>(17);

    // 右边玩家的牌
    private ArrayList<Card> rightCards = new ArrayList<Card>(17);

    // 底牌
    private ArrayList<Card> topCards = new ArrayList<Card>(3);

    // 底牌副本
    private ArrayList<Card> copyOfTopCards = new ArrayList<Card>(3);

    // 所有的牌
    private ArrayList<Card> allCards = new ArrayList<Card>(54);

    // 地主
    private int diZhu = 0;

    private static Logger logger = Logger.getLogger(DealingDevice.class
            .getName());

    public DealingDevice() {
        deal();
    }

    /**
     * 初始化,洗牌，发牌
     */
    private void deal() {
        shuffle();
        divide();
    }

    //发牌
    private void divide() {
        // 1号玩家的牌
        for (int j = 0; j < 17; j++) {
            Card card = allCards.get(j * 3);
            myCards.add(card);
            if (myCards.get(j).id == 1) {
                diZhu = 1;
            }
        }

        // 2号玩家的牌
        for (int j = 0; j < 17; j++) {
            Card card = allCards.get(j * 3 + 1);
            rightCards.add(card);

            if (rightCards.get(j).id == 1) {
                diZhu = 2;
            }
        }

        // 3号玩家的牌
        for (int j = 0; j < 17; j++) {
            Card card = allCards.get(j * 3 + 2);
            leftCards.add(card);

            if (leftCards.get(j).id == 1) {
                diZhu = 3;
            }
        }

        for (int i = 51; i < 54; i++) {

            Card card = allCards.get(i);
            topCards.add(card);

            Card Card = new Card(card.id);

            copyOfTopCards.add(Card);
        }

        CardUtil.sortCards(myCards);
        CardUtil.sortCards(leftCards);
        CardUtil.sortCards(rightCards);
    }

    /**
     * 洗牌
     */
    private void shuffle() {

        for (int i = 0; i < 54; i++) {
            Card card = new Card(i + 1);
            allCards.add(card);
        }

        System.out.println("洗牌之前：");
        CardUtil.printCards(allCards);

        // 洗牌,交换1000次
        for (int i = 0; i <= 1000; i++) {
            Collections.shuffle(allCards);// 打乱牌的位置
        }

        System.out.println("洗牌之后：");
        CardUtil.printCards(allCards);
    }

    /**
     * 打印牌
     */
    private void printCards() {
        System.out.println("玩家1的牌：");
        CardUtil.printCards(myCards);

        System.out.println("玩家2的牌：");
        CardUtil.printCards(leftCards);

        System.out.println("玩家3的牌：");
        CardUtil.printCards(rightCards);

        System.out.println("底牌：");
        CardUtil.printCards(topCards);
    }

    public ArrayList<Card> getAllCards() {
        return allCards;
    }

    public ArrayList<Card> getCopyOfTopCards() {
        return copyOfTopCards;
    }

    public int getDiZhu() {
        return diZhu;
    }

    public ArrayList<Card> getLeftCards() {
        return leftCards;
    }

    public ArrayList<Card> getMyCards() {
        return myCards;
    }

    public ArrayList<Card> getRightCards() {
        return rightCards;
    }

    public ArrayList<Card> getTopCards() {
        return topCards;
    }

    public static void main(String[] args) {
        DealingDevice one = new DealingDevice();
        one.printCards();

        logger.info("没有排序的牌：");
        ArrayList<Card> allCards2 = one.getAllCards();
        CardUtil.printCards(allCards2);

        logger.info("排序之后的牌：");
        CardUtil.sortCards(allCards2);
        CardUtil.printCards(allCards2);
    }

}
