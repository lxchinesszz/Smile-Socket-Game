package smile.protocol;

/**
 * @Package: com.netty
 * @Description:
 * @author: liuxin
 * @date: 2018/3/16 下午6:10
 */
public  class Protocol {
    /**
     * 主要: 区分发送方
     * 1 为客户端请求
     * 2 为服务端响应
     */
    private byte main;
    /**
     * 副号
     * 区分协议类型
     */
    private byte sub;

    public Protocol(byte main, byte sub) {
        this.main = main;
        this.sub = sub;
    }

    public Protocol(int main, int sub) {
       this((byte)main,(byte)sub);
    }

    public Protocol() {
    }

    public int getMain() {
        return main;
    }

    public void setMain(byte main) {
        this.main = main;
    }

    public int getSub() {
        return sub;
    }

    public void setSub(byte sub) {
        this.sub = sub;
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "main=" + main +
                ", sub=" + sub +
                '}';
    }
}
