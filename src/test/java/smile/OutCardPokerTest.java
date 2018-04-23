package smile;

import smile.service.home.Player;
import smile.service.poker.CardPoker;

import java.util.Arrays;
import java.util.List;

/**
 * @Package: smile
 * @Description: ${todo}
 * @date: 2018/4/22 下午11:56
 * @author: liuxin
 */
public class OutCardPokerTest {
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            fapaiTest();
        }

    }

    public static void fapaiTest(){
        System.err.println("---------start----------");
        Player p1 = new Player("1", "liuxin");
        Player p2 = new Player("2", "baobiao");
        Player p3 = new Player("3", "yangfan");
        Player p4 = new Player("4", "yangyang");
        List<Player> players = Arrays.asList(p1, p2, p3, p4);
        CardPoker cardPoker = new CardPoker(4);
        cardPoker.pokerShuffle();
        cardPoker.deal(players);
        players.parallelStream().forEach(OutCardPokerTest::printPlayerCard);
        System.err.println("---------end----------");
    }

    public static void printPlayerCard(Player player) {
        System.err.println("牌数量：" + player.getPoker().size());
        System.out.println(player.getName() + "当前手中的牌:" + player.getPoker());
    }
}
