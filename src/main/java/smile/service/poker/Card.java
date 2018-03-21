package smile.service.poker;

/**
 * @Package: com.example.poker.poker2
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/11 下午8:19
 */
public class Card implements Comparable {

    private static final long serialVersionUID = -8641665718074590961L;

    // 一张牌的大类型
    public enum CardBigType {
        HEI_TAO, HONG_TAO, MEI_HUA, FANG_KUAI, XIAO_WANG, DA_WANG
    }

    // 一张牌的小类型
    public enum CardSmallType {
        A, ER, SAN, SI, WU, LIU, QI, BA, JIU, SHI, J, Q, K, XIAO_WANG, DA_WANG
    }

    // 牌的数字ID
    public int id;

    // 牌的大类型
    public final CardBigType bigType;

    // 牌的小类型
    public final CardSmallType smallType;

    // 牌的等级
    public int grade;


    // 通过牌的整型id构造一张牌
    public Card(int id) {
        this.id = id;
        bigType = CardUtil.getBigType(id);
        smallType = CardUtil.getSmallType(id);
        grade = CardUtil.getGrade(id);
    }

    // 方便查看一张牌，格式为"方块A(A0.gif),方块2(20.gif)"
    @Override
    public String toString() {
        String str = "";
        if (bigType != null) {
            str += CardUtil.typeToZi(bigType);
        }
        if (smallType != null) {
            str += CardUtil.typeToZi(smallType);
        }
        return str;
    }

    /**
     * 0代表13
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        Card card = (Card) o;
        int ord = CardUtil.getSmallType(id).ordinal();
        int pre_ord = CardUtil.getSmallType(card.id).ordinal();
        return ord-pre_ord;
    }
}

