package smile.service.poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @Package: com.example.poker.poker2
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/11 下午8:41
 */
public final class GameRule {

    private GameRule() {

    }

    public static Logger logger = Logger.getLogger(GameRule.class.getName());

    /**
     * 判断我选择出的牌和上家的牌的大小，决定是否可以出牌
     *
     * @param myCards      我想出的牌
     * @param myCardType   我的牌的类型
     * @param prevCards    上家的牌
     * @param prevCardType 上家的牌型
     * @return 可以出牌，返回true；否则，返回false。
     */
    public static boolean isOvercomePrev(List<Card> myCards,
                                         CardType myCardType, List<Card> prevCards, CardType prevCardType) {
        // 我的牌和上家的牌都不能为null
        if (myCards == null || prevCards == null) {
            return false;
        }

        if (myCardType == null || prevCardType == null) {
            logger.info("上家出的牌不合法，所以不能出。");
            return false;
        }

        // 上一首牌的个数
        int prevSize = prevCards.size();
        int mySize = myCards.size();

        // 我先出牌，上家没有牌
        if (prevSize == 0 && mySize != 0) {
            return true;
        }

        // 集中判断是否王炸，免得多次判断王炸
        if (prevCardType == CardType.WANG_ZHA) {
            logger.info("上家王炸，肯定不能出。");
            return false;
        } else if (myCardType == CardType.WANG_ZHA) {
            logger.info("我王炸，肯定能出。");
            return true;
        }

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (prevCardType != CardType.ZHA_DAN && myCardType == CardType.ZHA_DAN) {
            return true;
        }

        // 默认情况：上家和自己想出的牌都符合规则
        CardUtil.sortCards(myCards);// 对牌排序
        CardUtil.sortCards(prevCards);// 对牌排序

        int myGrade = myCards.get(0).grade;
        int prevGrade = prevCards.get(0).grade;

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        // 单
        if (prevCardType == CardType.DAN && myCardType == CardType.DAN) {
            // 一张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);
        }
        // 对子
        else if (prevCardType == CardType.DUI_ZI
                && myCardType == CardType.DUI_ZI) {
            // 2张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);

        }
        // 3不带
        else if (prevCardType == CardType.SAN_BU_DAI
                && myCardType == CardType.SAN_BU_DAI) {
            // 3张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);
        }
        // 炸弹
        else if (prevCardType == CardType.ZHA_DAN
                && myCardType == CardType.ZHA_DAN) {
            // 4张牌可以大过上家的牌
            return compareGrade(myGrade, prevGrade);

        }
        // 3带1
        else if (prevCardType == CardType.SAN_DAI_YI
                && myCardType == CardType.SAN_DAI_YI) {

            // 3带1只需比较第2张牌的大小
            myGrade = myCards.get(1).grade;
            prevGrade = prevCards.get(1).grade;
            return compareGrade(myGrade, prevGrade);

        }
        // 4带2
        else if (prevCardType == CardType.SI_DAI_ER
                && myCardType == CardType.SI_DAI_ER) {

            // 4带2只需比较第3张牌的大小
            myGrade = myCards.get(2).grade;
            prevGrade = prevCards.get(2).grade;

        }
        // 顺子
        else if (prevCardType == CardType.SHUN_ZI
                && myCardType == CardType.SHUN_ZI) {
            if (mySize != prevSize) {
                return false;
            } else {
                // 顺子只需比较最大的1张牌的大小
                myGrade = myCards.get(mySize - 1).grade;
                prevGrade = prevCards.get(prevSize - 1).grade;
                return compareGrade(myGrade, prevGrade);
            }

        }
        // 连对
        else if (prevCardType == CardType.LIAN_DUI
                && myCardType == CardType.LIAN_DUI) {
            if (mySize != prevSize) {
                return false;
            } else {
                // 顺子只需比较最大的1张牌的大小
                myGrade = myCards.get(mySize - 1).grade;
                prevGrade = prevCards.get(prevSize - 1).grade;
                return compareGrade(myGrade, prevGrade);
            }

        }
        // 飞机
        else if (prevCardType == CardType.FEI_JI
                && myCardType == CardType.FEI_JI) {
            if (mySize != prevSize) {
                return false;
            } else {
                // 顺子只需比较第5张牌的大小(特殊情况333444555666没有考虑，即12张的飞机，可以有2种出法)
                myGrade = myCards.get(4).grade;
                prevGrade = prevCards.get(4).grade;
                return compareGrade(myGrade, prevGrade);
            }
        }

        // 默认不能出牌
        return false;
    }

    /**
     * 判断我所有的牌中，是否存在能够管住上家的牌，决定出牌按钮是否显示
     *
     * @param myCards      我所有的牌 *
     * @param prevCards    上家的牌
     * @param prevCardType 上家牌的类型
     * @return 可以出牌，返回true；否则，返回false。
     */
    public static boolean isOvercomePrev(List<Card> myCards,
                                         List<Card> prevCards, CardType prevCardType) {
        // 我的牌和上家的牌都不能为null
        if (myCards == null || prevCards == null) {
            return false;
        }

        if (prevCardType == null) {
            System.out.println("上家出的牌不合法，所以不能出。");
            return false;
        }

        // 默认情况：上家和自己想出的牌都符合规则
        CardUtil.sortCards(myCards);// 对牌排序
        CardUtil.sortCards(prevCards);// 对牌排序

        // 上一首牌的个数
        int prevSize = prevCards.size();
        int mySize = myCards.size();

        // 我先出牌，上家没有牌
        if (prevSize == 0 && mySize != 0) {
            return true;
        }

        // 集中判断是否王炸，免得多次判断王炸
        if (prevCardType == CardType.WANG_ZHA) {
            System.out.println("上家王炸，肯定不能出。");
            return false;
        }

        if (mySize >= 2) {
            List<Card> cards = new ArrayList<Card>();
            cards.add(new Card(myCards.get(mySize - 1).id));
            cards.add(new Card(myCards.get(mySize - 2).id));
            if (isDuiWang(cards)) {
                return true;
            }
        }

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (prevCardType != CardType.ZHA_DAN) {
            if (mySize < 4) {
                return false;
            } else {
                for (int i = 0; i < mySize - 3; i++) {
                    int grade0 = myCards.get(i).grade;
                    int grade1 = myCards.get(i + 1).grade;
                    int grade2 = myCards.get(i + 2).grade;
                    int grade3 = myCards.get(i + 3).grade;

                    if (grade1 == grade0 && grade2 == grade0
                            && grade3 == grade0) {
                        return true;
                    }
                }
            }

        }

        int prevGrade = prevCards.get(0).grade;

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        // 上家出单
        if (prevCardType == CardType.DAN) {
            // 一张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 0; i--) {
                int grade = myCards.get(i).grade;
                if (grade > prevGrade) {
                    // 只要有1张牌可以大过上家，则返回true
                    return true;
                }
            }

        }
        // 上家出对子
        else if (prevCardType == CardType.DUI_ZI) {
            // 2张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 1; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;

                if (grade0 == grade1) {
                    if (grade0 > prevGrade) {
                        // 只要有1对牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出3不带
        else if (prevCardType == CardType.SAN_BU_DAI) {
            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;

                if (grade0 == grade1 && grade0 == grade2) {
                    if (grade0 > prevGrade) {
                        // 只要3张牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出3带1
        else if (prevCardType == CardType.SAN_DAI_YI) {
            // 3带1 3不带 比较只多了一个判断条件
            if (mySize < 4) {
                return false;
            }

            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;

                if (grade0 == grade1 && grade0 == grade2) {
                    if (grade0 > prevGrade) {
                        // 只要3张牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出炸弹
        else if (prevCardType == CardType.ZHA_DAN) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;
                int grade3 = myCards.get(i - 3).grade;

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    if (grade0 > prevGrade) {
                        // 只要有4张牌可以大过上家，则返回true
                        return true;
                    }
                }
            }

        }
        // 上家出4带2
        else if (prevCardType == CardType.SI_DAI_ER) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;
                int grade3 = myCards.get(i - 3).grade;

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    // 只要有炸弹，则返回true
                    return true;
                }
            }
        }
        // 上家出顺子
        else if (prevCardType == CardType.SHUN_ZI) {
            if (mySize < prevSize) {
                return false;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Card> cards = new ArrayList<Card>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(new Card(myCards.get(i - j).grade));
                    }

                    CardType myCardType = getCardType(cards);
                    if (myCardType == CardType.SHUN_ZI) {
                        int myGrade2 = cards.get(cards.size() - 1).grade;// 最大的牌在最后
                        int prevGrade2 = prevCards.get(prevSize - 1).grade;// 最大的牌在最后

                        if (myGrade2 > prevGrade2) {
                            return true;
                        }
                    }
                }
            }

        }
        // 上家出连对
        else if (prevCardType == CardType.LIAN_DUI) {
            if (mySize < prevSize) {
                return false;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Card> cards = new ArrayList<Card>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(new Card(myCards.get(i - j).grade));
                    }

                    CardType myCardType = getCardType(cards);
                    if (myCardType == CardType.LIAN_DUI) {
                        int myGrade2 = cards.get(cards.size() - 1).grade;// 最大的牌在最后,getCardType会对列表排序
                        int prevGrade2 = prevCards.get(prevSize - 1).grade;// 最大的牌在最后

                        if (myGrade2 > prevGrade2) {
                            return true;
                        }
                    }
                }
            }

        }
        // 上家出飞机
        else if (prevCardType == CardType.FEI_JI) {
            if (mySize < prevSize) {
                return false;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Card> cards = new ArrayList<Card>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(new Card(myCards.get(i - j).grade));
                    }

                    CardType myCardType = getCardType(cards);
                    if (myCardType == CardType.FEI_JI) {
                        int myGrade4 = cards.get(4).grade;//
                        int prevGrade4 = prevCards.get(4).grade;//

                        if (myGrade4 > prevGrade4) {
                            return true;
                        }
                    }
                }
            }
        }

        // 默认不能出牌
        return false;
    }

    /**
     * 比较2个grade的大小
     *
     * @param grade1
     * @param grade2
     * @return
     */
    private static boolean compareGrade(int grade1, int grade2) {
        return grade1 > grade2;
    }

    /**
     * 检测牌的类型
     *
     * @param myCards 我出的牌
     * @return 如果遵守规则，返回牌的类型，否则，返回null。
     */
    public static CardType getCardType(List<Card> myCards) {
        CardType cardType = null;
        if (myCards != null) {
            // 大概率事件放前边，提高命中率
            if (isDan(myCards)) {
                cardType = CardType.DAN;
            } else if (isDuiWang(myCards)) {
                cardType = CardType.WANG_ZHA;
            } else if (isDuiZi(myCards)) {
                cardType = CardType.DUI_ZI;
            } else if (isZhaDan(myCards)) {
                cardType = CardType.ZHA_DAN;
            } else if (isSanDaiYi(myCards) != -1) {
                cardType = CardType.SAN_DAI_YI;
            } else if (isSanBuDai(myCards)) {
                cardType = CardType.SAN_BU_DAI;
            } else if (isShunZi(myCards)) {
                cardType = CardType.SHUN_ZI;
            } else if (isLianDui(myCards)) {
                cardType = CardType.LIAN_DUI;
            } else if (isSiDaiEr(myCards)) {
                cardType = CardType.SI_DAI_ER;
            } else if (isFeiJi(myCards)) {
                cardType = CardType.FEI_JI;
            }
        }

        return cardType;

    }

    /**
     * 判断牌是否为单
     *
     * @param myCards 牌的集合
     * @return 如果为单，返回true；否则，返回false。
     */
    public static boolean isDan(List<Card> myCards) {
        // 默认不是单
        boolean flag = false;
        if (myCards != null && myCards.size() == 1) {
            flag = true;
        }
        return flag;
    }

    /**
     * 判断牌是否为对子
     *
     * @param myCards 牌的集合
     * @return 如果为对子，返回true；否则，返回false。
     */
    public static boolean isDuiZi(List<Card> myCards) {
        // 默认不是对子
        boolean flag = false;

        if (myCards != null && myCards.size() == 2) {

            int grade1 = myCards.get(0).grade;
            int grade2 = myCards.get(1).grade;
            if (grade1 == grade2) {
                flag = true;
            }
        }

        return flag;

    }

    /**
     * 判断牌是否为3带1
     *
     * @param myCards 牌的集合
     * @return 如果为3带1，被带牌的位置，0或3，否则返回-1。炸弹返回-1。
     */
    public static int isSanDaiYi(List<Card> myCards) {
        int flag = -1;
        // 默认不是3带1
        if (myCards != null && myCards.size() == 4) {
            // 对牌进行排序
            CardUtil.sortCards(myCards);

            int[] grades = new int[4];
            grades[0] = myCards.get(0).grade;
            grades[1] = myCards.get(1).grade;
            grades[2] = myCards.get(2).grade;
            grades[3] = myCards.get(3).grade;

            // 暂时认为炸弹不为3带1
            if ((grades[1] == grades[0]) && (grades[2] == grades[0])
                    && (grades[3] == grades[0])) {
                return -1;
            }
            // 3带1，被带的牌在牌头
            else if ((grades[1] == grades[0] && grades[2] == grades[0])) {
                return 0;
            }
            // 3带1，被带的牌在牌尾
            else if (grades[1] == grades[3] && grades[2] == grades[3]) {
                return 3;
            }
        }
        return flag;
    }

    /**
     * 判断牌是否为3不带
     *
     * @param myCards 牌的集合
     * @return 如果为3不带，返回true；否则，返回false。
     */
    public static boolean isSanBuDai(List<Card> myCards) {
        // 默认不是3不带
        boolean flag = false;

        if (myCards != null && myCards.size() == 3) {
            int grade0 = myCards.get(0).grade;
            int grade1 = myCards.get(1).grade;
            int grade2 = myCards.get(2).grade;

            if (grade0 == grade1 && grade2 == grade0) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断牌是否为顺子
     *
     * @param myCards 牌的集合
     * @return 如果为顺子，返回true；否则，返回false。
     */
    public static boolean isShunZi(List<Card> myCards) {
        // 默认是顺子
        boolean flag = true;

        if (myCards != null) {

            int size = myCards.size();
            // 顺子牌的个数在5到12之间
            if (size < 4 || size > 12) {
                return false;
            }

            // 对牌进行排序
            CardUtil.sortCards(myCards);

            for (int n = 0; n < size - 1; n++) {
                int prev = myCards.get(n).grade;
                int next = myCards.get(n + 1).grade;
                // 小王、大王、2不能加入顺子
                if (prev == 17 || prev == 16 || prev == 15 || next == 17
                        || next == 16 || next == 15) {
                    flag = false;
                    break;
                } else {
                    if (prev - next != -1) {
                        flag = false;
                        break;
                    }

                }
            }
        }

        return flag;
    }

    /**
     * 判断牌是否为炸弹
     *
     * @param myCards 牌的集合
     * @return 如果为炸弹，返回true；否则，返回false。
     */
    public static boolean isZhaDan(List<Card> myCards) {
        // 默认不是炸弹
        boolean flag = false;
        if (myCards != null && myCards.size() == 4) {

            int[] grades = new int[4];
            grades[0] = myCards.get(0).grade;
            grades[1] = myCards.get(1).grade;
            grades[2] = myCards.get(2).grade;
            grades[3] = myCards.get(3).grade;
            if ((grades[1] == grades[0]) && (grades[2] == grades[0])
                    && (grades[3] == grades[0])) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断牌是否为王炸
     *
     * @param myCards 牌的集合
     * @return 如果为王炸，返回true；否则，返回false。
     */
    public static boolean isDuiWang(List<Card> myCards) {
        // 默认不是对王
        boolean flag = false;

        if (myCards != null && myCards.size() == 2) {

            int gradeOne = myCards.get(0).grade;
            int gradeTwo = myCards.get(1).grade;

            // 只有小王和大王的等级之后才可能是33
            if (gradeOne + gradeTwo == 33) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 判断牌是否为连对
     *
     * @param myCards 牌的集合
     * @return 如果为连对，返回true；否则，返回false。
     */
    public static boolean isLianDui(List<Card> myCards) {
        // 默认是连对
        boolean flag = true;
        if (myCards == null) {
            flag = false;
            return flag;
        }

        int size = myCards.size();
//        if (size < 6 || size % 2 != 0)
        if (size < 4 || size % 2 != 0) {
            flag = false;
        } else {
            // 对牌进行排序
            CardUtil.sortCards(myCards);
            for (int i = 0; i < size; i = i + 2) {
                if (myCards.get(i).grade != myCards.get(i + 1).grade) {
                    flag = false;
                    break;
                }

                if (i < size - 2) {
                    if (myCards.get(i).grade - myCards.get(i + 2).grade != -1) {
                        flag = false;
                        break;
                    }
                }
            }
        }

        return flag;
    }

    /**
     * 判断牌是否为飞机
     *
     * @param myCards 牌的集合
     * @return 如果为飞机，返回true；否则，返回false。
     */
    public static boolean isFeiJi(List<Card> myCards) {
        boolean flag = false;
        // 默认不是单
        if (myCards != null) {

            int size = myCards.size();
            if (size >= 6) {
                // 对牌进行排序
                CardUtil.sortCards(myCards);

                if (size % 3 == 0 && size % 4 != 0) {
                    flag = isFeiJiBuDai(myCards);
                } else if (size % 3 != 0 && size % 4 == 0) {
                    flag = isFeiJiDai(myCards);
                } else if (size == 12) {
                    flag = isFeiJiBuDai(myCards) || isFeiJiDai(myCards);
                }
            }
        }
        return flag;
    }

    /**
     * 判断牌是否为飞机不带
     *
     * @param myCards 牌的集合
     * @return 如果为飞机不带，返回true；否则，返回false。
     */
    public static boolean isFeiJiBuDai(List<Card> myCards) {
        if (myCards == null) {
            return false;
        }

        int size = myCards.size();
        int n = size / 3;

        int[] grades = new int[n];

        if (size % 3 != 0) {
            return false;
        } else {
            for (int i = 0; i < n; i++) {
                if (!isSanBuDai(myCards.subList(i * 3, i * 3 + 3))) {
                    return false;
                } else {
                    // 如果连续的3张牌是一样的，记录其中一张牌的grade
                    grades[i] = myCards.get(i * 3).grade;
                }
            }
        }

        for (int i = 0; i < n - 1; i++) {
            if (grades[i] == 15) {// 不允许出现2
                return false;
            }

            if (grades[i + 1] - grades[i] != 1) {
                System.out.println("等级连续,如 333444"
                        + (grades[i + 1] - grades[i]));
                return false;// grade必须连续,如 333444
            }
        }

        return true;
    }

    /**
     * 判断牌是否为飞机带
     *
     * @param myCards 牌的集合
     * @return 如果为飞机带，返回true；否则，返回false。
     */
    public static boolean isFeiJiDai(List<Card> myCards) {
        int size = myCards.size();
        int n = size / 4;// 此处为“除”，而非取模
        int i = 0;
        for (i = 0; i + 2 < size; i = i + 3) {
            int grade1 = myCards.get(i).grade;
            int grade2 = myCards.get(i + 1).grade;
            int grade3 = myCards.get(i + 2).grade;
            if (grade1 == grade2 && grade3 == grade1) {

                // return isFeiJiBuDai(myCards.subList(i, i + 3 *
                // n));8张牌时，下标越界,subList不能取到最后一个元素
                ArrayList<Card> cards = new ArrayList<Card>();
                for (int j = i; j < i + 3 * n; j++) {// 取字串
                    cards.add(myCards.get(j));
                }
                return isFeiJiBuDai(cards);
            }

        }

        return false;
    }

    /**
     * 判断牌是否为4带2
     *
     * @param myCards 牌的集合
     * @return 如果为4带2，返回true；否则，返回false。
     */
    public static boolean isSiDaiEr(List<Card> myCards) {
        boolean flag = false;
        if (myCards != null && myCards.size() == 6) {

            // 对牌进行排序
            CardUtil.sortCards(myCards);
            for (int i = 0; i < 3; i++) {
                int grade1 = myCards.get(i).grade;
                int grade2 = myCards.get(i + 1).grade;
                int grade3 = myCards.get(i + 2).grade;
                int grade4 = myCards.get(i + 3).grade;

                if (grade2 == grade1 && grade3 == grade1 && grade4 == grade1) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 打印一个字符串，方便调式
     *
     * @param str 要打印的字符串
     */
    public static void print(String str) {
        System.out.println(str);
    }

    public static void print(Card card) {
        System.out.println(card);
    }


    public static void main(String[] args) {
        List list = new ArrayList();
        list.add(new Card(5));
        list.add(new Card(5));
        list.add(new Card(6));
        list.add(new Card(6));
        list.add(new Card(7));
        list.add(new Card(7));
        System.out.println(isLianDui(list));
    }

    /**
     * 获取是自己手中可以压住上一个家的牌信息
     *
     * @param myCards      我所有的牌 *
     * @param prevCards    上家的牌
     * @param prevCardType 上家牌的类型
     * @return 可以出牌，返回true；否则，返回false。
     */
    public static List<Card> getOvercomePrevCards(List<Card> myCards,
                                                  List<Card> prevCards, CardType prevCardType) {
        // 我的牌和上家的牌都不能为null
        if (myCards != null & prevCards == null) {
            return Arrays.asList(myCards.get(0));
        }

        if (myCards == null & prevCards == null) {
            return null;
        }

        if (prevCardType == null) {
            System.out.println("上家出的牌不合法，所以不能出。");
            return Arrays.asList(myCards.get(0));
        }

        // 默认情况：上家和自己想出的牌都符合规则
        CardUtil.sortCards(myCards);// 对牌排序
        CardUtil.sortCards(prevCards);// 对牌排序

        // 上一首牌的个数
        int prevSize = prevCards.size();
        int mySize = myCards.size();

        // 我先出牌，上家没有牌,我可以随便出牌
        if (prevSize == 0 && mySize != 0) {
            return Arrays.asList(myCards.get(0));
        }

        // 集中判断对方不是炸弹，我出炸弹的情况
        if (prevCardType != CardType.ZHA_DAN) {
            if (mySize < 4) {
                return null;
            } else {
                for (int i = 0; i < mySize - 3; i++) {
                    int grade0 = myCards.get(i).grade;
                    int grade1 = myCards.get(i + 1).grade;
                    int grade2 = myCards.get(i + 2).grade;
                    int grade3 = myCards.get(i + 3).grade;

                    if (grade1 == grade0 && grade2 == grade0
                            && grade3 == grade0) {
                        return Arrays.asList(myCards.get(i),myCards.get(i+1),myCards.get(i+2),myCards.get(i+3));
                    }
                }
            }

        }

        int prevGrade = prevCards.get(0).grade;

        // 比较2家的牌，主要有2种情况，1.我出和上家一种类型的牌，即对子管对子；
        // 2.我出炸弹，此时，和上家的牌的类型可能不同
        // 王炸的情况已经排除

        // 上家出单
        if (prevCardType == CardType.DAN) {
            // 一张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 0; i--) {
                int grade = myCards.get(i).grade;
                if (grade > prevGrade) {
                    // 只要有1张牌可以大过上家，则返回true
                    return Arrays.asList(myCards.get(i));
                }
            }

        }

        // 上家出对子
        else if (prevCardType == CardType.DUI_ZI) {
            // 2张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 1; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                if (grade0 == grade1) {
                    if (grade0 > prevGrade) {
                        // 只要有1对牌可以大过上家，则返回true
                        return Arrays.asList(myCards.get(i), myCards.get(i - 1));
                    }
                }
            }

        }
        // 上家出3不带
        else if (prevCardType == CardType.SAN_BU_DAI) {
            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;

                if (grade0 == grade1 && grade0 == grade2) {
                    if (grade0 > prevGrade) {
                        // 只要3张牌可以大过上家，则返回true
                        return Arrays.asList(myCards.get(i), myCards.get(i - 1), myCards.get(i - 2));
                    }
                }
            }

        }
        // 上家出3带1
        else if (prevCardType == CardType.SAN_DAI_YI) {
            // 3带1 3不带 比较只多了一个判断条件
            if (mySize < 4) {
                return null;
            }
            //三带一的单张
            Card card = prevCards.get(3);
            // 3张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 2; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;

                if (grade0 == grade1 && grade0 == grade2) {
                    if (grade0 > prevGrade) {
                        List<Card> new_poker=new ArrayList<>();
                        new_poker.addAll(myCards);
                        new_poker.removeAll(Arrays.asList(myCards.get(i), myCards.get(i - 1), myCards.get(i - 2)));
                        for (Card c:new_poker){
                            int grade = c.grade;
                            if (grade>card.grade){
                                // 只要3张牌可以大过上家，则返回true
                                return Arrays.asList(myCards.get(i), myCards.get(i - 1), myCards.get(i - 2),c);
                            }
                        }

                    }
                }
            }

        }
        // 上家出炸弹
        else if (prevCardType == CardType.ZHA_DAN) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;
                int grade3 = myCards.get(i - 3).grade;

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    if (grade0 > prevGrade) {
                        // 只要有4张牌可以大过上家，则返回true
                        return Arrays.asList(myCards.get(i), myCards.get(i - 1), myCards.get(i - 2), myCards.get(i - 3));
                    }
                }
            }

        }
        // 上家出4带2
        else if (prevCardType == CardType.SI_DAI_ER) {
            // 4张牌可以大过上家的牌
            for (int i = mySize - 1; i >= 3; i--) {
                int grade0 = myCards.get(i).grade;
                int grade1 = myCards.get(i - 1).grade;
                int grade2 = myCards.get(i - 2).grade;
                int grade3 = myCards.get(i - 3).grade;

                if (grade0 == grade1 && grade0 == grade2 && grade0 == grade3) {
                    // 只要有炸弹，则返回true
                    return Arrays.asList(myCards.get(i), myCards.get(i - 1), myCards.get(i - 2), myCards.get(i - 3));
                }
            }
        }
        // 上家出顺子
        else if (prevCardType == CardType.SHUN_ZI) {
            if (mySize < prevSize) {
                return null;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Card> cards = new ArrayList<Card>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(new Card(myCards.get(i - j).grade));
                    }

                    CardType myCardType = getCardType(cards);
                    if (myCardType == CardType.SHUN_ZI) {
                        int myGrade2 = cards.get(cards.size() - 1).grade;// 最大的牌在最后
                        int prevGrade2 = prevCards.get(prevSize - 1).grade;// 最大的牌在最后
                        if (myGrade2 > prevGrade2) {
                            return cards;
                        }
                    }
                }
            }

        }
        // 上家出连对
        else if (prevCardType == CardType.LIAN_DUI) {
            if (mySize < prevSize) {
                return null;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Card> cards = new ArrayList<Card>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(new Card(myCards.get(i - j).grade));
                    }

                    CardType myCardType = getCardType(cards);
                    if (myCardType == CardType.LIAN_DUI) {
                        int myGrade2 = cards.get(cards.size() - 1).grade;// 最大的牌在最后,getCardType会对列表排序
                        int prevGrade2 = prevCards.get(prevSize - 1).grade;// 最大的牌在最后
                        if (myGrade2 > prevGrade2) {
                            return cards;
                        }
                    }
                }
            }

        }
        // 上家出飞机
        else if (prevCardType == CardType.FEI_JI) {
            if (mySize < prevSize) {
                return null;
            } else {
                for (int i = mySize - 1; i >= prevSize - 1; i--) {
                    List<Card> cards = new ArrayList<Card>();
                    for (int j = 0; j < prevSize; j++) {
                        cards.add(new Card(myCards.get(i - j).grade));
                    }

                    CardType myCardType = getCardType(cards);
                    if (myCardType == CardType.FEI_JI) {
                        int myGrade4 = cards.get(4).grade;//
                        int prevGrade4 = prevCards.get(4).grade;//

                        if (myGrade4 > prevGrade4) {
                            return cards;
                        }
                    }
                }
            }
        }

        // 默认不能出牌
        return null;
    }
}

