package smile.config;

/**
 * @Package: smile.config
 * @Description:
 * @date: 2018/4/21 下午2:35
 * @author: liuxin
 */
public enum OperatorEnum {
    /**
     * 当前操作状态
     * 叫地主 = 1,
     * 不叫 = 2,
     * 抢地主 = 3,
     * 不抢 = 4,
     * 加倍 = 5,
     * 不加倍 = 6,
     * 思考中 = 7,
     * 不出 = 8,
     * 出牌 = 9,
     * 其他 = 10
     */
    JIAODIZHU(1),
    BUJIAO(2),
    QIANGDIZHU(3),
    BUQIANG(4),
    JIABEI(5),
    BUJIAOBEI(6),
    SIKAOZHONG(7),
    BUCHU(8),
    CHUPAI(9),
    OTHER(-1);

    public int status;

    OperatorEnum(int status) {
        this.status = status;
    }

}
