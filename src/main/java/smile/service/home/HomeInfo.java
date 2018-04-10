package smile.service.home;

import lombok.Data;
import lombok.ToString;

/**
 * @Package: com.example.poker
 * @Description: 房间类型
 * @author: liuxin
 * @date: 2018/3/10 下午9:01
 */
@Data
@ToString
public class HomeInfo {

    /**
     * 房主
     */
    private Player homeOwner;
    /**
     * 最大房间人数
     */
    private int personNum;

    /**
     * 一幅扑克牌
     */
    private Poker poker;

    /**
     * 房间局数
     */
    private String roomNum;

    /**
     * 剩余局数
     */
    private int shengyuRoomNum;

    /**
     * 最大倍数
     */
    private String multiple;

    /**
     * 抵注
     */
    private String blind;

    /**
     *
     */
    private String sharedIP;

    /**
     *
     */
    private String AA;

    /**
     *
     */
    private String method;

    public HomeInfo(Player homeOwner, String max, Poker poker,String multiple,
                    String blind,String sharedIP,String AA,String method,String roomNum) {
        this(homeOwner, Integer.parseInt(max), poker,multiple,blind,sharedIP,AA,method,roomNum);
    }

    public HomeInfo(Player homeOwner, int max, Poker poker,String multiple,
                    String blind,String sharedIP,String AA,String method,String roomNum) {
        this.homeOwner = homeOwner;
        if (max < 3 || max > 5) {
            //最大人数不能大于6,小于3
            throw new IllegalArgumentException("Largest number is not greater than 5, less than 3");
        }
        this.personNum = max;
        this.poker = poker;
        this.multiple=multiple;
        this.blind=blind;
        this.sharedIP=sharedIP;
        this.AA=AA;
        this.method=method;
        this.roomNum=roomNum;
        this.shengyuRoomNum=Integer.parseInt(roomNum);
    }


    public void subShengyuJvshu(){
        this.shengyuRoomNum-=1;
    }

    /**
     * 其他
     * eg:
     * 1. 房间样式
     */


    public Player getHomeOwner() {
        return homeOwner;
    }

    public void setHomeOwner(Player homeOwner) {
        this.homeOwner = homeOwner;
    }

    public int getPersonNum() {
        return personNum;
    }

    public void setPersonNum(int personNum) {
        this.personNum = personNum;
    }

    public String getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }

    public String getMultiple() {
        return multiple;
    }

    public void setMultiple(String multiple) {
        this.multiple = multiple;
    }

    public String getBlind() {
        return blind;
    }

    public void setBlind(String blind) {
        this.blind = blind;
    }

    public String getSharedIP() {
        return sharedIP;
    }

    public void setSharedIP(String sharedIP) {
        this.sharedIP = sharedIP;
    }

    public String getAA() {
        return AA;
    }

    public void setAA(String AA) {
        this.AA = AA;
    }

    /**
     * 0: 抢地主
     * 1: 四个二比较必抢
     * 2: 头压头,尾压尾
     * 3: 明底牌
     * (0是否,1,1,1,明底牌)
     * @param opera
     * @return
     */
    public boolean getMethod(int opera) {
        String[] split =null;
        if (method!=null){
           split = method.split(",");
        }
        return split[opera].equalsIgnoreCase("1");
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Poker getPoker() {
        return poker;
    }

    public void setPoker(Poker poker) {
        this.poker = poker;
    }
}
