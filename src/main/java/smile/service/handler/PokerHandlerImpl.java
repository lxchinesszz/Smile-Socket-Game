package smile.service.handler;

import smile.service.home.Home;
import smile.service.home.Player;
import smile.service.home.Poker;

import java.util.List;

/**
 * @Package: com.example.poker
 * @Description: 处理发牌逻辑
 * @author: liuxin
 * @date: 2018/3/10 下午9:33
 */
public class PokerHandlerImpl implements GameHandler {

    private Poker poker;

    private  List<Player> players;

    public PokerHandlerImpl(Home home) {
        this.poker = home.getPoker();
        this.players = home.getPlayers();
    }

    /**
     * 发牌
     */
    public void deal() {
        poker.deal(players);
    }

    /**
     * 洗牌
     */
    public void shuffle(){
        poker.pokerShuffle();
    }
}
