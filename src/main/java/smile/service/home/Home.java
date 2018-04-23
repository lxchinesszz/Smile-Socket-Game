package smile.service.home;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.database.dto.DizhuS2C_DTO;
import smile.database.dto.OperatorS2C_DTO;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.service.handler.PlayerInfoNotify;
import smile.service.poker.Card;
import smile.service.poker.CardType;
import smile.service.poker.CardUtil;
import smile.service.poker.GameRule;
import smile.tool.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ToString
public class Home {
    Logger logger = LoggerFactory.getLogger(Home.class);

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private ListeningScheduledExecutorService listeningScheduledExecutor = MoreExecutors.listeningDecorator(scheduledExecutorService);

    private ListenableScheduledFuture schedule;

    private Map<String, ListenableScheduledFuture> scheduledFutureMap;

    List<PlayerTimerTaskInfo> timerTaskInfoList = new ArrayList<>();

    public List<PlayerTimerTaskInfo> getTimerTaskInfoList() {
        return timerTaskInfoList;
    }

    public void setTimerTaskInfoList(List<PlayerTimerTaskInfo> timerTaskInfoList) {
        this.timerTaskInfoList = timerTaskInfoList;
    }

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
    private String homeName = "斗地主包房";
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

    /**
     * 当前该操作玩家的座位号
     */
    private volatile int currentChairId = -1;
    /**
     * 地主的座位号
     */
    private volatile int landLordChairId = -1;
    /**
     * 初始化地主座位号
     */
    private volatile int initLordChairId = -1;
    /**
     * 操作次数
     */
    private volatile int operaCount;
    /**
     * 抢地主的次数
     */
    private volatile int qiangDiZhuCount;
    /**
     * 倍数
     */
    private int multiple = 1;
    /**
     * 第一个抢地主的座位号
     */
    private String FirstJiaoDizhuCharid;

    /**
     * 最后一个抢地主的玩家
     */
    private int lastQiangDiZhuChairId = -1;
    /**
     * 房间开始时间
     */
    private long startTime;
    /**
     * 房间结束时间
     */
    private long endTime;

    /**
     * 当前桌面上剩余的牌
     */
    private List<Card> currentCards;
    /**
     * 当前出牌的玩家
     */
    private Player currentOutCardsPlayer;

    /**
     * 是否最大人出牌
     */
    private boolean isMaxCardOut = false;
    /**
     * 是否下一个玩家出牌
     */
    private boolean isNextOutCard = false;

    /**
     * 初始化地主的第一次操作
     */
    private int initLordChairIdStatus = -1;


    public int getLastQiangDiZhuChairId() {
        return lastQiangDiZhuChairId;
    }

    public void setLastQiangDiZhuChairId(int lastQiangDiZhuChairId) {
        this.lastQiangDiZhuChairId = lastQiangDiZhuChairId;
    }

    public int getInitLordChairIdStatus() {
        return initLordChairIdStatus;
    }

    public void setInitLordChairIdStatus(int initLordChairIdStatus) {
        this.initLordChairIdStatus = initLordChairIdStatus;
    }

    public int getInitLordChairId() {
        return initLordChairId;
    }

    public void setInitLordChairId(int initLordChairId) {
        this.initLordChairId = initLordChairId;
    }

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
        if (FirstJiaoDizhuCharid == null) {
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

    public void clearFirstJiaoDiZhu(){
        this.FirstJiaoDizhuCharid=null;
    }

    public void setFirstJiaoDizhuCharid(String firstQiangDizhuCharid) {
        if (FirstJiaoDizhuCharid == null) {
            FirstJiaoDizhuCharid = firstQiangDizhuCharid;
        }
    }

    public void addQiangDiZhuCount() {
        this.qiangDiZhuCount += 1;
    }

    public void setQiangDiZhuCount(int qiangDiZhuCount) {
        this.qiangDiZhuCount = qiangDiZhuCount;
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

    public void setOperaCount(int num) {
        this.operaCount = num;
    }

    public int getOperaCount() {
        return this.operaCount;
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

    public int getPreOperaCharId(int currentChairId) {
        return currentChairId > 0 ? currentChairId - 1 : 3;
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
        //判断游戏是否开始
        if (readyNum >= homeInfo.getPersonNum()) {
            //当游戏开始，则开始定时器
//            timeOutListen();
        }
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

    public String threadToString() {
        System.err.println("房间id: " + hid);
        System.err.println("房间名称: " + homeName);
        System.err.println("当前桌面牌: " + currentCards);
        System.err.println("当前最大出牌人: " + getCurrentOutCardsPlayer());
        System.err.println("当前房间倍数: " + multiple);
        System.err.println("当前地主: " + getPlayerByChairId(landLordChairId));
        System.err.println("当前牌面底牌: " + poker.getMainPoker());
        System.err.println("当前操作次数: " + this.operaCount);
        System.err.println("当前抢地主次数: " + this.qiangDiZhuCount);
        System.err.println("当前操作的玩家:" + this.getCurrentChairId());
        List<Player> players = getPlayers();
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            Player player = players.get(i);
            System.err.println(player);
        }
        return "";
    }

    public Home getHome() {
        return this;
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
        System.err.println("当前操作的玩家:" + this.getCurrentChairId());
        List<Player> players = getPlayers();
        for (int i = 0, lenth = players.size(); i < lenth; i++) {
            Player player = players.get(i);
            System.err.println(player);
        }
        return "";
    }

    public int getOtherPlayerOperaStatus(String paichuUid) {
        int operaStatus = 0;
        List<Player> players = getPlayers();
        for (int i = 0; i < 4; i++) {
            Player player = players.get(i);
            if (player.getChairId().equalsIgnoreCase(paichuUid)) {
                continue;
            }
            int operatorStatus = player.getOperatorStatus();
            operaStatus += operatorStatus;
        }
        return operaStatus;
    }

    public PlayerTimerTask playerTimerTask() {
        final SocketPackage socketPackage = new SocketPackage(new Protocol(2, 12));
        PlayerInfoNotify playerInfoNotify = IOC.get().getBean(PlayerInfoNotify.class);
        PlayerTimerTask timerTask = new PlayerTimerTask() {
            @Override
            public void run() {
                System.err.println("进入定时任务外层");
                //获取当前操作的座位号
                int currentChairId = getCurrentChairId();
                //初始化的地主,和当前操作的人一样
                int initLordChairId = getInitLordChairId();
                //获取当前操作的玩家
                Player ownPlayer = getPlayerByChairId(currentChairId);
                System.err.println(threadToString());
                //判断是否是超时，默认是超时，当超时，就替他出牌
                if (ownPlayer.isTimeOut()) {
                    logger.info("超时执行");
                    //判断是否存在地主
                    int landLordChairId = getLandLordChairId();
                    int firstQiangDizhuCharid = getFirstQiangDizhuCharid();
                    int otherPlayerOperaStatus = getOtherPlayerOperaStatus(currentChairId + "");
                    //说明还没人叫地主，替这个人叫地主,并通知下一个人叫地主
                    if (currentChairId == initLordChairId) {
                        int operatorStatus = ownPlayer.getOperatorStatus();
                        if (operatorStatus != 1 || operatorStatus != 2) {
                            //标识为操作,通知所有人
                            int nextOperaChairId = getNextOperaChairId(Integer.parseInt(ownPlayer.getChairId()));
                            OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                                    .currentChairId(getNextOperaChairId(Integer.parseInt(ownPlayer.getChairId())) + "")
                                    .currentStatus("1")
                                    .preCharid(ownPlayer.getChairId()).pokers(new ArrayList<>(1))
                                    .operationStatus("2")
                                    //因为还未出牌，所以最大玩家为-1
                                    .maxOperaCharId("-1")
                                    .build();
                            //3.
//                            getPlayerByChairId(nextOperaChairId).setTimeOut(true,);
                            ownPlayer.setOperatorStatus(2);
                            addOperaCount();
                            socketPackage.setProtocol(new Protocol(2, 12));
                            socketPackage.setDatagram(operatorS2CDto);
                            playerInfoNotify.operator(getPlayers(), socketPackage);
                            logger.info("超时操作");
                            return;
                        }
                        if (firstQiangDizhuCharid <= 3) {
                            //说明当前已经有人抢地主
                            //通知下一个人操抢地主
                            //封装操作
                            addQiangDiZhuCount();
                            OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                                    //下一个操作的玩家
                                    .currentChairId(getNextOperaChairId(currentChairId) + "")
                                    //出牌操作，通知下一个玩家抢地主
                                    .currentStatus("3")
                                    //上一个操作的玩家就是自己
                                    .preCharid(currentChairId + "")
                                    //服务器替出的牌
                                    .pokers(new ArrayList<>(1))
                                    //如果上一个玩家是叫地主，当前玩家超时就该为不叫
                                    .operationStatus("2")
                                    //因为还未出牌，所以最大玩家为-1
                                    .maxOperaCharId("-1")
                                    .build();
                            ownPlayer.setOperatorStatus(2);
                            socketPackage.setDatagram(operatorS2CDto);
                            playerInfoNotify.operator(getPlayers(), socketPackage);
                            logger.info("超时操作");
                        }
                    } else if (landLordChairId == -1 & otherPlayerOperaStatus == 6) {
                        //TODO 当前玩家是第一个地主，其他玩家都不叫 //标识为操作,通知所有人
                        //通知其他人抢地主,自己是叫地主
                        //当前玩家是地主，
                        socketPackage.setProtocol(new Protocol(2, 13));
                        ArrayList<Card> mainPoker = getPoker().getMainPoker();
                        List<String> mainPokers = new ArrayList<>();
                        mainPoker.stream().forEach(card -> {
                            mainPokers.add(card.id + "");
                        });
                        DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(initLordChairId + "").pokers(mainPokers).build();
                        ownPlayer.setOperatorStatus(1);
                        addOperaCount();
                        socketPackage.setDatagram(dizhuS2C_dto);
                        playerInfoNotify.operator(getPlayers(), socketPackage);
                        setLandLordChairId(initLordChairId);
                        ArrayList<Card> mainPokerCard = getPoker().getMainPoker();
                        getPlayerByChairId(landLordChairId).addMainPoker(mainPokerCard);
                        logger.info("超时操作");

                        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                                .currentChairId(initLordChairId + "")
                                .currentStatus("9")
                                .preCharid(currentChairId + "").pokers(new ArrayList<>(1))
                                .operationStatus("-1")
                                //因为还未出牌，所以最大玩家为-1
                                .maxOperaCharId("-1")
                                .build();
                        //3.
                        ownPlayer.setOperatorStatus(2);
                        addOperaCount();
                        socketPackage.setProtocol(new Protocol(2, 12));
                        socketPackage.setDatagram(operatorS2CDto);
                        playerInfoNotify.operator(getPlayers(), socketPackage);

                        //TODO 获取到地主给他添加一个超时,因为所有人的超时任务都已经取消了，要重新添加
                        Player playerByChairId = getPlayerByChairId(landLordChairId);
                        playerByChairId.setTimeOut(true, getHome());
                        return;
                    } else if (landLordChairId == -1) {
                        //通知下一个人操叫地主
                        //封装操作
                        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                                //下一个操作的玩家
                                .currentChairId(getNextOperaChairId(currentChairId) + "")
                                //出牌操作，通知下一个玩家操作出牌
                                .currentStatus("1")
                                //上一个操作的玩家就是自己
                                .preCharid(currentChairId + "")
                                //服务器替出的牌
                                .pokers(new ArrayList<>(1))
                                //当前玩家如果是超时就是不叫
                                .operationStatus("2")
                                //因为还未出牌，所以最大玩家为-1
                                .maxOperaCharId("-1")
                                .build();
                        ownPlayer.setOperatorStatus(2);
                        addOperaCount();
                        socketPackage.setDatagram(operatorS2CDto);
                        playerInfoNotify.operator(getPlayers(), socketPackage);
                        logger.info("超时操作");
                    } else {
                        //当玩家第一次出牌
                        //获取当前牌面上的牌
                        List<Card> currentCards = getCurrentCards();
                        //上一个出牌的牌的类型
                        CardType preCardType = GameRule.getCardType(currentCards);
                        //获取我手中的牌
                        List<Card> myPokers = ownPlayer.getPoker();
                        //将要出牌的
                        List<Card> overcomePrevCards = GameRule.getOvercomePrevCards(myPokers, currentCards, preCardType);
                        if (overcomePrevCards != null) {
                            //桌面最大的牌更新
                            setCurrentCards(overcomePrevCards);
                            //设置当前最大出牌的玩家
                            setCurrentOutCardsPlayer(ownPlayer);
                        }
                        //封装操作
                        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                                //下一个操作的玩家
                                .currentChairId(getNextOperaChairId(currentChairId) + "")
                                //出牌操作，通知下一个玩家操作出牌
                                .currentStatus("9")
                                //上一个操作的玩家就是自己
                                .preCharid(currentChairId + "")
                                //服务器替出的牌
                                .pokers(CardUtil.cardConvert(overcomePrevCards))
                                //通知当期玩家是出牌-》服务器替出牌
                                .operationStatus("9")
                                //因为还未出牌，所以最大玩家为-1
                                .maxOperaCharId(getCurrentOutCardsPlayer().getChairId())
                                .build();
                        socketPackage.setDatagram(operatorS2CDto);
                        playerInfoNotify.operator(getPlayers(), socketPackage);
                        logger.info("超时操作");
                    }
                }
            }
        };
        return timerTask;
    }

    /**
     * 1. 一个定时器每隔30s 扫描当前桌面该操作的玩家
     * 是否存在超时，如果存在就替他操作，并将桌面下一家操作的玩家，设置为当前桌面操作咱俩，添加超时。
     * 2. 玩家默认都是超时。
     * <p>
     * 3. 当玩家主动操作后将咱俩超时取消，当该咱俩操作，将超时打开。
     */

    public void timeOutListen() {
        if (timerTaskInfoList.size() == 0) {
            timerTaskInfoList.add(new PlayerTimerTaskInfo(false, 15000, playerTimerTask()));
        }
        PlayerTimer timer = new PlayerTimer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int size = timerTaskInfoList.size();
                    if (size > 1) {
                        throw new RuntimeException("同一时刻，定时任务不能大于1");
                    }
                    PlayerTimerTaskInfo timerTaskInfo = timerTaskInfoList.get(0);
                    if (timerTaskInfo == null) {
                        return;
                    }
                    long delay = timerTaskInfo.getDelay();
                    PlayerTimerTask timerTask = timerTaskInfo.getTimerTask();
                    if (!timerTaskInfo.isExe()) {
                        timer.scheduleAtFixedRate(timerTask, delay, delay);
                        //已经添加的任务不会重复添加,一个房间最多只有一个任务
                        timerTaskInfo.setExe(true);
                    }
                }
            }
        }).start();
    }
}

