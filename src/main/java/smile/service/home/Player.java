package smile.service.home;

import lombok.Data;
import lombok.ToString;
import smile.service.poker.Card;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @Package: com.example.poker
 * @Description: //斗地主玩家
 * @author: liuxin
 * @date: 2018/3/10 下午8:28
 */
@Data
@ToString
public class Player {
    //用户id
    private String uid;
    private String name;//玩家姓名
    private ArrayList<Card> poker;//玩家手中的牌
    private Channel channel;
    /**
     * 座位id
     */
    private String chairId;

    /**
     * 当前操作状态
     * 叫地主 = 1,
     * 不叫 = 2,
     * 抢地主 = 3,
     * 不抢 = 4,
     * 加倍 = 5,
     * 不加倍 = 6,
     * 思考中 = 7,
     * 不出 = 8,
     * 出牌 = 9,
     */
    private int operatorStatus;


    /**
     * 玩家状态
     * 旁观 LookOn = 1,  等待状态（已经准备） Wait = 2,   正在游戏 Play = 3,   断线状态 OffLine = 4
     */
    private String status;


    /**
     * 是否叫地主
     */
    private boolean jiaodizhu=false;

    public boolean isJiaodizhu() {
        return jiaodizhu;
    }

    public void setJiaodizhu(boolean jiaodizhu) {
        this.jiaodizhu = jiaodizhu;
    }

    public Player(Player player) {
        this.uid = player.getUid();
        this.name = player.getName();
        this.poker = player.getPoker();
        this.channel = player.channel;
        this.chairId = player.getChairId();
        this.status = player.getStatus();
    }

    public void updatePlayer(Player player) {
        this.uid = player.getUid();
        this.name = player.getName();
        this.poker = player.getPoker();
        this.channel = player.channel;
        this.chairId = player.getChairId();
        this.status = player.getStatus();
    }

    public Player(String uid, String name) {
        this(uid, name, null);
    }

    public Player(String uid, String name, Channel channel) {
        this.uid = uid;
        this.name = name;
        this.channel = channel;
        poker = new ArrayList();
        this.status = "1";
        this.chairId = "-1";
    }

    public ArrayList<Card> getPoker() {
        Collections.sort(poker);
        return poker;
    }

    public void removePoker(List<Card> cards) {
        for (Card card0 : cards) {
            poker.removeIf(new Predicate<Card>() {
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
        }
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

    @Override
    public String toString() {
        return "Player{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", poker=" + poker +
                ", channel=" + channel +
                ", chairId='" + chairId + '\'' +
                ", operatorStatus=" + operatorStatus +
                ", status='" + status + '\'' +
                '}';
    }
}
