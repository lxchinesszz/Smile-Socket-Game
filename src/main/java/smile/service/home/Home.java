package smile.service.home;

import java.util.ArrayList;
import java.util.List;

public class Home {

    /**
     * 房间类型
     */
    private HomeInfo homeInfo;
    /**
     * 房间号: 6位
     */
    private int hid;
    /**
     * 房间名
     */
    private String homeName;
    /**
     * 房间人数
     */
    List<Player> players;

    /**
     * 房间扑克
     */
    private Poker poker;

    public Home(int hid) {
        this.hid = hid;
    }

    public Home(HomeInfo homeInfo, int hid) {
        this.homeInfo = homeInfo;
        this.hid = hid;
        //设置最大人数
        players = new ArrayList(homeInfo.getMax());
        players.add(homeInfo.getHomeOwner());
        this.poker=homeInfo.getPoker();
    }


    public int getHid() {
        return hid;
    }

    public void setHid(int hid) {
        this.hid = hid;
    }

    public String getHomeName() {
        return homeName;
    }

    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayers(Player player) {
        players.add(player);
    }

    public HomeInfo getHomeInfo() {
        return homeInfo;
    }

    public void setHomeInfo(HomeInfo homeInfo) {
        this.homeInfo = homeInfo;
    }

    public Poker getPoker() {
        return poker;
    }

    public void setPoker(Poker poker) {
        this.poker = poker;
    }
}

