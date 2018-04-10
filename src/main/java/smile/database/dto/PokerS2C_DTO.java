package smile.database.dto;


import lombok.Data;
import smile.protocol.Datagram;
import smile.service.poker.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: liuxin
 * @date: 2018/3/25 下午8:59
 */
@Data
public class PokerS2C_DTO implements Datagram {
    private List<String> pokers;
    private String landLordChairId;
    private String currentChairId;

    public void addPoker(List<Card> poker,int chairId,int currentChairId){
        if (pokers==null){
            pokers=new ArrayList<>();
        }
        for (Card card:poker) {
            pokers.add(String.valueOf(card.id));
        }
        Collections.sort(poker);
        this.currentChairId=String.valueOf(currentChairId);
        setChairId(String.valueOf(chairId));
    }

    public void setChairId(String chairId){
        this.landLordChairId=chairId;
    }

    @Override
    public String toString() {
        return "PokerS2C_DTO{" +
                "pokers=" + pokers +
                ", landLordChairId='" + landLordChairId + '\'' +
                ", currentChairId='" + currentChairId + '\'' +
                '}';
    }
}
