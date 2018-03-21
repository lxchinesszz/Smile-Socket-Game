package smile.service.home;

/**
 * @Package: com.example.poker
 * @Description: 房间类型
 * @author: liuxin
 * @date: 2018/3/10 下午9:01
 */
public class HomeInfo {

    /**
     * 房主
     */
    private Player homeOwner;
    /**
     * 最大房间类型
     */
    private int max;

    /**
     * 一幅扑克牌
     */
    private Poker poker;

    public HomeInfo(Player homeOwner, int max,Poker poker) {
        this.homeOwner = homeOwner;
        if (max<3||max>5){
            //最大人数不能大于6,小于3
            throw new IllegalArgumentException("Largest number is not greater than 5, less than 3");
        }
        this.max = max;
        this.poker=poker;
    }


    /**
     * 其他
     * eg:
     *  1. 房间样式
     */



    public Player getHomeOwner() {
        return homeOwner;
    }

    public void setHomeOwner(Player homeOwner) {
        this.homeOwner = homeOwner;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public Poker getPoker() {
        return poker;
    }

    public void setPoker(Poker poker) {
        this.poker = poker;
    }
}
