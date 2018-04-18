package smile.service.home;

import com.google.common.util.concurrent.*;
import lombok.ToString;
import smile.service.poker.Card;
import smile.tool.SnatchDiZhu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;

@ToString
public class Home {

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private ListeningScheduledExecutorService listeningScheduledExecutor = MoreExecutors.listeningDecorator(scheduledExecutorService);

    private ListenableScheduledFuture schedule;

    private Map<String, ListenableScheduledFuture> scheduledFutureMap;

    /**
     * 房间类型
     */
    private HomeInfo homeInfo;
    /**
     * 房间号: 6位
     */
    private String hid;
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
    /**
     * 当前已经准备的人数
     */
    private volatile int readyNum;

    private volatile int currentChairId=-1;

    private volatile int landLordChairId=-1;

    private volatile int operaCount;

    private volatile int qiangDiZhuCount;

    private int multiple = 1;

    private String FirstJiaoDizhuCharid;

    private long startTime;
    private long endTime;

    private List<Card> currentCards;

    private Player currentOutCardsPlayer;

    private boolean isMaxCardOut = false;
    //是否下一个出牌
    private boolean isNextOutCard = false;




    public int getCurrentChairId() {
        return currentChairId;
    }

    public void setCurrentChairId(int currentChairId) {
        this.currentChairId = currentChairId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setReadyNum(int readyNum) {
        this.readyNum = readyNum;
    }

    public int getFirstQiangDizhuCharid() {
        if (FirstJiaoDizhuCharid==null){
            return 1233432423;
        }
        return Integer.parseInt(FirstJiaoDizhuCharid);
    }

    /**
     * 只有第一次会保存
     *
     * @param firstQiangDizhuCharid
     */
    public void setFirstJiaoDizhuCharid(int firstQiangDizhuCharid) {
        if (FirstJiaoDizhuCharid == null) {
            FirstJiaoDizhuCharid = String.valueOf(firstQiangDizhuCharid);
        }
    }

    public void setFirstJiaoDizhuCharid(String firstQiangDizhuCharid) {
        if (FirstJiaoDizhuCharid == null) {
            FirstJiaoDizhuCharid = firstQiangDizhuCharid;
        }
    }

    public void addQiangDiZhuCount() {
        this.qiangDiZhuCount += 1;
    }

    public int getQiangDiZhuCount() {
        return qiangDiZhuCount;
    }

    public boolean isDizhu() {
        return this.qiangDiZhuCount % 5 == 0;
    }


    public void addOperaCount() {
        this.operaCount += 1;
    }

    /**
     * 判断是否一轮
     *
     * @return
     */
    public boolean isAgain() {
        return this.operaCount % 5 == 0;
    }

    /**
     * 判断是否下一个人出牌
     *
     * @return
     */
    public boolean isNextOutCard() {
        return true;
    }

    public void multiply(int num) {
        this.multiple = this.multiple * num;
    }

    public int getMultiple() {
        return multiple;
    }




    public void setNextOutCard(boolean nextOutCard) {
        isNextOutCard = nextOutCard;
    }

    public boolean isMaxCardOut() {
        return isMaxCardOut;
    }

    public void setMaxCardOut(boolean maxCardOut) {
        isMaxCardOut = maxCardOut;
    }

    public Player getCurrentOutCardsPlayer() {
        return currentOutCardsPlayer;
    }

    public void setCurrentOutCardsPlayer(Player currentOutCardsPlayer) {
        this.currentOutCardsPlayer = currentOutCardsPlayer;
    }

    public List<Card> getCurrentCards() {
        return currentCards;
    }

    public void setCurrentCards(List<Card> currentCards) {
        this.currentCards = currentCards;
    }

    public boolean isOutCard() {
        int flagStatus = 0;
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            Player player = players.get(i);
            if (Integer.parseInt(player.getChairId()) == this.currentChairId) {
                continue;
            }
            if (player.getOperatorStatus() >= 5) {
                flagStatus += 5;
            }
        }
        if (flagStatus >= 15) {
            return true;
        }
        return false;
    }

    public String getChairdByUid(String uid) {
        return getPlayer(uid).getChairId();
    }


    public Player getPlayerByChairId(int landLordChairId) {
        Player player = null;
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            player = players.get(i);
            if (Integer.parseInt(player.getChairId()) == landLordChairId) {
                player = players.get(i);
                break;
            }
        }
        return player;
    }


    public int getNextOperaChairId(int landLordChairId) {
        this.currentChairId = landLordChairId == homeInfo.getPersonNum() - 1 ? 0 : landLordChairId + 1;
        return this.currentChairId;
    }

    public int getPreOperaCharId(int currentChairId){
       return currentChairId>0?currentChairId-1:3;
    }

    public int getLandLordChairId() {
        return landLordChairId;
    }

    public void setLandLordChairId(int landLordChairId) {
        this.landLordChairId = landLordChairId;
    }

    /**
     * 添加监听事件
     *
     * @param runnable
     * @param time
     * @param callback
     */
    public void listeningSchedule(Player player, Runnable runnable, int time, FutureCallback callback) {
        if (scheduledFutureMap == null) {
            scheduledFutureMap = new ConcurrentHashMap();
        }
        schedule = listeningScheduledExecutor.schedule(runnable, time, TimeUnit.SECONDS);
        Futures.addCallback(schedule, callback);
        scheduledFutureMap.put(player.getChairId(), schedule);
    }

    public ListenableScheduledFuture getSchedule(Player player) {
        return this.scheduledFutureMap.get(player.getChairId());
    }


    /**
     * 取消定时
     *
     * @return
     */
    public boolean cancle(Player player) {
        return this.scheduledFutureMap.get(player.getChairId()).cancel(true);
    }

    /**
     * 获取下一个操作的人作为号码
     *
     * @return
     */
//    public int getCurrentChairId() {
//        return this.currentChairId;
//    }
    public boolean isExit() {
        return this.players.size() == 0;
    }

    public void addReady() {
        this.readyNum += 1;
    }


    public void subReady() {
        this.readyNum -= 1;
    }

    public boolean isStart() {
        return readyNum >= homeInfo.getPersonNum();
    }

    public Home(int hid) {
        this.hid = String.valueOf(hid);
    }

    public Home(HomeInfo homeInfo, int hid) {
        this.homeInfo = homeInfo;
        this.hid = String.valueOf(hid);
        //设置最大人数
        players = new ArrayList(homeInfo.getPersonNum());
        players.add(homeInfo.getHomeOwner());
        this.poker = homeInfo.getPoker();
    }

    public boolean removePlayer(String uid) {
        return players.removeIf(new Predicate<Player>() {
            @Override
            public boolean test(Player player) {
                return player.getUid().equalsIgnoreCase(uid);
            }
        });
    }

    public String getHid() {
        return hid;
    }

    public void setHid(int hid) {
        this.hid = String.valueOf(hid);
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

    public void clearEveryOnePoker() {
        for (Player p : players) {
            p.getPoker().clear();
        }
    }

    public Player getPlayer(String uid) {
        Player currentPlayer = null;
        out:
        for (Player player : players) {
            if (player.getUid().equalsIgnoreCase(uid)) {
                currentPlayer = player;
                break out;
            }
        }
        return currentPlayer;
    }

    public void updatePlayer(Player player) {
        for (int i = 0; i < players.size(); i++) {
            Player curent = players.get(i);
            if (curent.getUid().equalsIgnoreCase(player.getUid())) {
                players.get(i).updatePlayer(player);
                break;
            }
        }
    }


    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    private void addPlayer(Player player) {
        if (this.players.size() > homeInfo.getPersonNum()) {
            return;
        }
        this.players.add(player);
    }

    public void addPlayers(Player player) {
        players.add(player);
    }

    public HomeInfo getHomeInfo() {
        return this.homeInfo;
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


    @Override
    public String toString() {
        System.err.println("房间id: " + hid);
        System.err.println("房间名称: " + homeName);
        System.err.println("当前桌面牌: " + currentCards);
        System.err.println("当前最大出牌人: " + getCurrentOutCardsPlayer());
        System.err.println("当前房间倍数: " + multiple);
        System.err.println("当前地主: " + getPlayerByChairId(landLordChairId));
        System.err.println("当前牌面底牌: " + poker.getMainPoker());
        System.err.println("当前操作次数: " + this.operaCount);
        System.err.println("当前抢地主次数: " + this.qiangDiZhuCount);
        System.err.println("当前操作的玩家:"+this.getCurrentChairId());
        List<Player> players = getPlayers();
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            Player player = players.get(i);
            System.err.println(player);
        }
        return "";
    }
}

