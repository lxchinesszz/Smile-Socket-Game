package smile.service.handler;

import junit.framework.TestCase;
import org.json.JSONObject;
import smile.service.home.*;
import smile.service.poker.CardPoker;

import java.util.List;
import java.util.Random;

/**
 * @Package: smile.service.handler
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/19 下午4:23
 */
public class GameHandlerTest extends TestCase {

    public static void game() {
        Player p1 = new Player("","刘备");
        Player p2 = new Player("","关羽");
        Player p3 = new Player("","张飞");
        Player p4 = new Player("","凤雏");

        //创建一副扑克牌
        Poker poker = new CardPoker(4);
        //创建一个房间秘钥生成器
        HomeGenerator homeGenerator=new HomeGeneratorImpl();
        //创建一个房管,对房间进行管理
        HomeManager homeManager = new HomeManagerImpl(homeGenerator);
        //设置房间信息
        HomeInfo homeInfo = new HomeInfo(p1, 3, poker,"1","1","1","1","1","1");
        Home home = homeManager.createHome(homeInfo);
        //返回六位房间号
        int hid = home.getHid();
        System.out.println("房间号:" + hid);
        home.addPlayers(p2);
        home.addPlayers(p3);
        home.addPlayers(p4);

        List<Player> players = home.getPlayers();
        Poker poker0 = home.getPoker();
        poker0.pokerShuffle();
        poker0.deal(players);
        seePoker(home.getPlayers());
        System.out.println(home);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");
        home.getPoker().getMainPoker().clear();
        home.getPlayers().stream().forEach(player -> player.getPoker().clear());
        home.setPoker(new CardPoker(4));
        Poker poker1 = home.getPoker();
        poker1.pokerShuffle();
        poker1.deal(players);
        seePoker(home.getPlayers());

        System.out.println(home);


//
//        GameHandler dealHandler = new PokerHandlerImpl(home);
//        //发牌
//        dealHandler.deal();
//
//        seePoker(home.getPlayers());

    }

    private static void seePoker(List<Player> players) {
        for (Player player : players) {
            System.out.println(player.getName() + "手中的牌是 :");
            for (int i = 0; i < player.getPoker().size(); i++) {
//                System.out.print( "第"+(i + 1) + "张牌是：" + player.getPoker().get(i)+",");
                System.out.print(player.getPoker().get(i) + ",");
            }
            System.out.println();
            System.out.println("--------------------------");
        }
    }


    public static void main(String[] args) throws Exception{
        game();
//
//        Player p1 = new Player("刘备");
//        Player p2 = new Player("关羽");
//        Player p3 = new Player("张飞");
//
//
//        List<Player> players = Arrays.asList(p1, p2, p3);
//        Poker poker=new CardPoker();
//        /**
//         * 洗牌
//         */
//        poker.pokerShuffle();
//        /**
//         * 发牌
//         */
//        poker.deal(players);
//
//        seePoker(players);

//
//        JSONObject jsonObject=new JSONObject();
//        JSONObject put = jsonObject.put("key", "name");
//        System.out.println(jsonObject.isNull("key"));
//
//
//        Random random = new Random();
//
//        for (int i = 0; i < 10; i++) {
//            int i1 = random.nextInt(4);
//            System.out.println(i1);
//        }

    }
}