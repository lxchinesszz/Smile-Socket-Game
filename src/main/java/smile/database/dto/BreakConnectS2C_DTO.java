package smile.database.dto;

import lombok.Data;
import lombok.ToString;
import smile.protocol.Datagram;
import smile.service.home.Home;
import smile.service.home.HomeInfo;
import smile.service.home.Player;
import smile.service.poker.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: mac
 * @date: 2018/4/15 下午9:30
 */
@Data
@ToString
public class BreakConnectS2C_DTO implements Datagram {
    //玩家的牌
    private List<String> pokers;
    //桌面上牌
    private List<String> currentPokers;

    private List<PokerCount> pokerCounts;
    /**
     * 底牌
     */
    private List<String> mainPokers;
    //地主座位号
    private String landLordChairId;
    //当期操作座位号
    private String currentOperaCharId;
    //当期操作座位号
    private String currentOperaStatus;
    //上一个操作座位
    private String preOperaCharId;
    //上一个操作状态
    private String preOperaStatus;





    public void addPokers(List<Card> poker) {
        if (pokers == null) {
            pokers = new ArrayList<>();
        }
        if (poker!=null) {
            for (Card card : poker) {
                pokers.add(String.valueOf(card.id));
            }
        }
    }

    public void addCurrentPokers(List<Card> poker){
        if (currentPokers == null) {
            currentPokers = new ArrayList<>();
        }
        if (poker!=null){
            for (Card card : poker) {
                currentPokers.add(String.valueOf(card.id));
            }
        }

    }


    public void addPokerCount(List<Player> players) {
        if (pokerCounts == null) {
            pokerCounts = new ArrayList<>();
        }
        for (Player player : players) {
            String charid = player.getChairId();
            String pokerCount = player.getPoker().size()+"";
            String uid=player.getUid();
            pokerCounts.add(new PokerCount(uid,charid, pokerCount));
        }
    }

}
