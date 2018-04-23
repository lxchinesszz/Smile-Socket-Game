package smile.config;

/**
 * @Package: smile.config
 * @Description:
 * @date: 2018/4/23 上午1:44
 * @author: liuxin
 */
public enum ErrorEnum {
    JIPAIQI_FAIL(-12,"记牌器已过期,或不存在"),
    CHUPAI_FEIFA(-11,"当前出牌非法"),
    JIESAN_FAIL(-10,"房间内还有其他玩家，不能解散当前房间"),
    WUQVAN(-9,"当前uid无权解散房间"),
    MANYUAN(-8,"当前房间人数已满,不能再加入"),
    YOUXIZHONG(-7,"您正处在游戏中,不允许退出"),
    CARD_BUGOU(-6,"房卡不足"),
    UNFOUND_UID(-5,"当前uid不存在"),
    KOU_FAIL(-4,"房卡扣除失败,请稍后再试"),
    CARD_BUZU(-3,"房卡数量不足,不能充值"),
    NO_GONGYINGSHANG(-2,"当前用户非供应商"),
    HOME_UNFOUNJD(-1, "当前房间号不存在,请重新创建");

    private int code;
    private String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
