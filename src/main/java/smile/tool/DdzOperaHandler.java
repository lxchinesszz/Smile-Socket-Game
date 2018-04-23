package smile.tool;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smileframework.ioc.bean.annotation.InsertBean;
import org.smileframework.ioc.bean.annotation.SmileComponent;
import smile.config.OperatorEnum;
import smile.database.dto.DizhuS2C_DTO;
import smile.database.dto.OperatorS2C_DTO;
import smile.protocol.Protocol;
import smile.protocol.SocketPackage;
import smile.service.handler.PlayerInfoNotify;
import smile.service.home.Home;
import smile.service.home.Player;
import smile.service.poker.Card;
import smile.service.poker.CardUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: smile.tool
 * @Description:
 * @date: 2018/4/21 下午2:13
 * @author: liuxin
 */
@SmileComponent
public class DdzOperaHandler {
    Logger logger = LoggerFactory.getLogger(DdzOperaHandler.class);
    private Home home;

    @InsertBean
    PlayerInfoNotify playerInfoNotify;


    public void Operate(int status, Home home) {
        this.home = home;
        if (status <= 4) {
            QiangDiZhu(status);
        } else if (status <= 6) {
            JiaBei(status);
        } else if (status <= 9) {
            ChuPai(status);
        }
    }

    /**
     * 上一个玩家的操作
     *
     * @param preStatus
     */
    public void QiangDiZhu(int preStatus) {
        SocketPackage socketPackage = new SocketPackage(new Protocol(2, 12));
        //获取当前操作的座位号
        int currentChairId = home.getCurrentChairId();
        //初始化的地主,和当前操作的人一样
        int initLordChairId = home.getInitLordChairId();
        //地主
        int lordChairId = -1;
        //获取当前操作的玩家
        Player currentPlayer = home.getPlayerByChairId(currentChairId);
        currentPlayer.setOperatorStatus(preStatus);
        OperatorEnum willOperatorStatus = OperatorEnum.OTHER;
        Player firstJiaoDizhuPlayer = null;
        //地主是否确定，不确定为-1
        int landLordChairId = home.getLandLordChairId();
        //地主未确定
        int operatorStatus = currentPlayer.getOperatorStatus();
        //地主是否确定
        boolean isDizhu = false;
        //下一个操作的玩家作为号
        int nextOperaChairId = -1;
        //操作次数+1
        home.addOperaCount();
        OperaModel operaModel = null;

        if (!home.isAgain()) {
            if (currentChairId == initLordChairId & landLordChairId == -1) {
                home.setInitLordChairIdStatus(preStatus);
            }
        }
        if (home.isAgain()) {
            if (home.getInitLordChairIdStatus() == 1 & preStatus == 4) {
                isDizhu = true;
                lordChairId = home.getLastQiangDiZhuChairId();
                if (lordChairId == -1) {
                    logger.info("只有第一个人叫地主，其他人不抢，地主为第一个人");
                    lordChairId = home.getInitLordChairId();
                }
                willOperatorStatus = OperatorEnum.CHUPAI;
                //下一个操作的玩家是通知地主出牌
                nextOperaChairId = lordChairId;
                operaModel = new OperaModel(true, nextOperaChairId,
                        willOperatorStatus, currentChairId + "", preStatus, lordChairId);
            } else if (home.getInitLordChairIdStatus() == 1 & preStatus == 3) {
                isDizhu = true;
                lordChairId = home.getInitLordChairId();
                willOperatorStatus = OperatorEnum.CHUPAI;
                nextOperaChairId = lordChairId;
                operaModel = new OperaModel(true, nextOperaChairId,
                        willOperatorStatus, nextOperaChairId + "", preStatus, lordChairId);
            } else if (home.getInitLordChairIdStatus() == 2 & preStatus == 3) {
                isDizhu = true;
                lordChairId = home.getFirstQiangDizhuCharid();
                willOperatorStatus = OperatorEnum.CHUPAI;
                nextOperaChairId = lordChairId;
                operaModel = new OperaModel(true, nextOperaChairId,
                        willOperatorStatus, nextOperaChairId + "", preStatus, lordChairId);
            } else if (home.getInitLordChairIdStatus() == 2 & preStatus == 4) {
                isDizhu = true;
                lordChairId = home.getLastQiangDiZhuChairId();
                if (lordChairId == -1) {
                    logger.info("只有第一个人叫地主，其他人不抢，地主为第一个人");
                    lordChairId = home.getInitLordChairId();
                }
                willOperatorStatus = OperatorEnum.CHUPAI;
                //下一个操作的玩家是通知地主出牌
                nextOperaChairId = lordChairId;
                operaModel = new OperaModel(true, nextOperaChairId,
                        willOperatorStatus, currentChairId + "", preStatus, lordChairId);
            }
        } else {
            int otherPlayerOperaStatus = home.getOtherPlayerOperaStatus(String.valueOf(currentChairId));
            switch (operatorStatus) {
                case 1:
                    //叫地主，下一个玩家将要抢地主
                    if (otherPlayerOperaStatus == 6) {
                        nextOperaChairId = currentChairId;
                        willOperatorStatus = OperatorEnum.CHUPAI;
                        lordChairId = currentChairId;
                        operaModel = new OperaModel(true, nextOperaChairId, willOperatorStatus, currentChairId + "", preStatus, lordChairId);
                    } else {
                        home.setFirstJiaoDizhuCharid(currentChairId);
                        firstJiaoDizhuPlayer = home.getPlayerByChairId(currentChairId);
                        firstJiaoDizhuPlayer.setOperatorStatus(1);
                        willOperatorStatus = OperatorEnum.QIANGDIZHU;
                        nextOperaChairId = home.getNextOperaChairId(currentChairId);
                        operaModel = new OperaModel(false, nextOperaChairId, willOperatorStatus, currentChairId, preStatus);
                    }
                    break;
                case 2:
                    //TODO 判断是不是所有人都是不叫
                    if (otherPlayerOperaStatus == 6) {
                        int firstQiangDizhuCharid = home.getFirstQiangDizhuCharid();
                        if (firstQiangDizhuCharid > 1000) {//所有人没人叫地主，那么地主就给初始化的地主
                            int initLordChairId1 = home.getInitLordChairId();
                            willOperatorStatus = OperatorEnum.CHUPAI;
                            nextOperaChairId = initLordChairId1;
                            operaModel = new OperaModel(true, nextOperaChairId, willOperatorStatus, "-1", -1, initLordChairId1);
                        } else {
                            //地主就是第一个叫地主的人
                            willOperatorStatus = OperatorEnum.CHUPAI;
                            nextOperaChairId = firstQiangDizhuCharid;
                            operaModel = new OperaModel(true, nextOperaChairId, willOperatorStatus, "-1", -1, firstQiangDizhuCharid);
                        }
                    } else {
                        //不叫，下一个玩家将要叫地主
                        willOperatorStatus = OperatorEnum.JIAODIZHU;
                        nextOperaChairId = home.getNextOperaChairId(currentChairId);
                        operaModel = new OperaModel(false, nextOperaChairId, willOperatorStatus, currentChairId, preStatus);
                    }
                    break;
                case 3:
                    //抢地主，下一个玩家将要抢地主
                    home.setLastQiangDiZhuChairId(currentChairId);
                    if (home.getOperaCount() % 4 == 0) {
                        //判断第一个人是否是叫地主,如果不交
                        int initLordChairIdStatus = home.getInitLordChairIdStatus();
                        int firstQiangDizhuCharid = home.getFirstQiangDizhuCharid();
                        if (initLordChairIdStatus == 2 & firstQiangDizhuCharid < 4) {//第一玩家不交,通知第一个叫地主的玩家去抢地主
                            willOperatorStatus = OperatorEnum.QIANGDIZHU;
                            operaModel = new OperaModel(false, firstQiangDizhuCharid, willOperatorStatus, currentChairId, preStatus);
                        } else if (initLordChairIdStatus == 1 & firstQiangDizhuCharid < 4) {
                            willOperatorStatus = OperatorEnum.QIANGDIZHU;
                            operaModel = new OperaModel(false, firstQiangDizhuCharid, willOperatorStatus, currentChairId, preStatus);
                        }
                    } else {
                        willOperatorStatus = OperatorEnum.QIANGDIZHU;
                        nextOperaChairId = home.getNextOperaChairId(currentChairId);
                        operaModel = new OperaModel(false, nextOperaChairId, willOperatorStatus, currentChairId, preStatus);
                    }
                    break;
                case 4:
                    //判断除了第一个叫地主的人其他人是不是都不请
                    int firstJiaoDizhuCharid = home.getFirstQiangDizhuCharid();
                    otherPlayerOperaStatus = home.getOtherPlayerOperaStatus(firstJiaoDizhuCharid + "");
                    int nextOperaPlayerChairId = nextOperaPlayerChairId(currentChairId);
                    int nextOperaStatus = home.getPlayerByChairId(nextOperaPlayerChairId).getOperatorStatus();
                    if (otherPlayerOperaStatus == 12) {
                        willOperatorStatus = OperatorEnum.CHUPAI;
                        nextOperaChairId = firstJiaoDizhuCharid;
                        operaModel = new OperaModel(true, nextOperaChairId, willOperatorStatus, "-1", -1, firstJiaoDizhuCharid);
                    } else if (nextOperaStatus == 0 & otherPlayerOperaStatus == 8) {
                        willOperatorStatus = OperatorEnum.QIANGDIZHU;
                        nextOperaChairId = home.getNextOperaChairId(currentChairId);
                        operaModel = new OperaModel(false, nextOperaChairId, willOperatorStatus, currentChairId, preStatus);
                    } else if (otherPlayerOperaStatus == 8) {//除了叫地主的玩家，其他3个玩家两个不叫，一个不抢
                        willOperatorStatus = OperatorEnum.CHUPAI;
                        nextOperaChairId = firstJiaoDizhuCharid;
                        operaModel = new OperaModel(true, nextOperaChairId, willOperatorStatus, currentChairId + "", preStatus, firstJiaoDizhuCharid);
                    } else if (otherPlayerOperaStatus == 10) {//除了叫地主的人，一个不叫，两个不抢
                        willOperatorStatus = OperatorEnum.CHUPAI;
                        nextOperaChairId = firstJiaoDizhuCharid;
                        operaModel = new OperaModel(true, nextOperaChairId, willOperatorStatus, currentChairId + "", preStatus, firstJiaoDizhuCharid);
                    } else {
                        //不抢，下一个玩家将要抢地主
                        willOperatorStatus = OperatorEnum.QIANGDIZHU;
                        nextOperaChairId = home.getNextOperaChairId(currentChairId);
                        operaModel = new OperaModel(false, nextOperaChairId, willOperatorStatus, currentChairId, preStatus);
                    }
                    break;
            }
        }
        //TODO 当地主确定
        if (operaModel.isDiZhu()) {
            socketPackage.setProtocol(new Protocol(2, 13));
            ArrayList<Card> mainPoker = home.getPoker().getMainPoker();
            List<String> mainPokers = CardUtil.cardConvert(mainPoker);
            DizhuS2C_DTO dizhuS2C_dto = DizhuS2C_DTO.builder().chaird(operaModel.getLordChairId() + "")
                    .pokers(mainPokers).build();
            socketPackage.setDatagram(dizhuS2C_dto);
            //通知所有玩家地主是谁
            playerInfoNotify.operator(home.getPlayers(), socketPackage);
            //设置地主
            home.setLandLordChairId(operaModel.getLordChairId());
            //将底牌添加到地主手牌中
            ArrayList<Card> mainPokerCard = home.getPoker().getMainPoker();
            home.getPlayerByChairId(operaModel.getLordChairId()).addMainPoker(mainPokerCard);
            //当抢到地主之后，将操作次数从零开始
            home.setOperaCount(0);
        }
        /**
         * 构建唯一操作
         */
        Player maxOperaPlayer = home.getCurrentOutCardsPlayer();
        String maxOperaCharId = maxOperaPlayer == null ? "-1" : maxOperaPlayer.getChairId();
        OperatorS2C_DTO operatorS2CDto = OperatorS2C_DTO.builder()
                //下一个将要操作的玩家
                .currentChairId(operaModel.nextOperaChairId + "")
                //下一个将要操作玩家的状态
                .currentStatus(String.valueOf(operaModel.getWillOperatorStatus().status))
                //上一个操作的玩家的座位号
                .preCharid(operaModel.getPreCharId()).pokers(new ArrayList<>(1))
                //上一个操作玩家的状态
                .operationStatus(operaModel.getPreStatus())
                .maxOperaCharId(maxOperaCharId)
                .build();
        socketPackage.setProtocol(new Protocol(2, 12));
        socketPackage.setDatagram(operatorS2CDto);
        playerInfoNotify.operator(home.getPlayers(), socketPackage);
    }

    public static int nextOperaPlayerChairId(int currentChairId) {
        return currentChairId == 4 - 1 ? 0 : currentChairId + 1;
    }


    public void JiaBei(int status) {

    }

    public void ChuPai(int status) {

    }

    @Data
    public class OperaModel {
        boolean isDiZhu;
        String nextOperaChairId;
        OperatorEnum willOperatorStatus;
        String preCharId;
        String preStatus;
        int lordChairId;

        public OperaModel(boolean isDiZhu, int nextOperaChairId, OperatorEnum willOperatorStatus, String preCharId, int preStatus, int lordChairId) {
            this.nextOperaChairId = String.valueOf(nextOperaChairId);
            this.willOperatorStatus = willOperatorStatus;
            this.preCharId = preCharId;
            this.preStatus = String.valueOf(preStatus);
            this.isDiZhu = isDiZhu;
            this.lordChairId = lordChairId;
        }

        public OperaModel(boolean isDiZhu, int nextOperaChairId, OperatorEnum willOperatorStatus, int preCharId, int preStatus) {
            this.nextOperaChairId = String.valueOf(nextOperaChairId);
            this.willOperatorStatus = willOperatorStatus;
            this.preCharId = String.valueOf(preCharId);
            this.preStatus = String.valueOf(preStatus);
            this.isDiZhu = isDiZhu;
        }

        public OperaModel(boolean isDiZhu, int nextOperaChairId, OperatorEnum willOperatorStatus, String preCharId, int preStatus) {
            this.nextOperaChairId = String.valueOf(nextOperaChairId);
            this.willOperatorStatus = willOperatorStatus;
            this.preCharId = preCharId;
            this.preStatus = String.valueOf(preStatus);
            this.isDiZhu = isDiZhu;
        }

    }

    public static void main(String[] args) {
        //TODO 获取 第一个地主的座位号，并以此，获取后面的几位
//        int firstChairId = initLordChairId;
//        int secondChairId=nextOperaPlayerChairId(firstChairId);
//        int thirdChairId=nextOperaPlayerChairId(secondChairId);
//        int fourthChairId=nextOperaPlayerChairId(thirdChairId);
        int firstChairId = 2;
        int secondChairId = nextOperaPlayerChairId(firstChairId);
        int thirdChairId = nextOperaPlayerChairId(secondChairId);
        int fourthChairId = nextOperaPlayerChairId(thirdChairId);
        System.out.println(firstChairId);
        System.out.println(secondChairId);
        System.out.println(thirdChairId);
        System.out.println(fourthChairId);


        //13333 初始化地主是地主   13334 最后一个是地主    13343 初始化地主是地主 13344 最后一个抢地主的是地主
        //13433 初始化地主是地主   13434 最后一个是地主    13443 初始化地主是地主 13444 最后一个抢地主的是地主

        //14333 初始化地主是地主   14334 最后一个是地主    14343 初始化地主是地主 14344 最后一个抢地主的是地主
        //14433 初始化地主是地主   14434 最后一个是地主    1444 初始化地主是地主 1444  初始化地主是地主


        //2133.3 第一个叫地主的是地主,并通知第一个叫地主的人最后选择   21334     21343  21344
        //21433   21434     21443  21443
        //22133   22134     22143  22144
        //22213   22214     22221  22221

        //            int secondChairId = nextOperaPlayerChairId(firstChairId);
//            int secondChairIdStatus = home.getPlayerByChairId(secondChairId).getOperatorStatus();
//            int thirdChairId = nextOperaPlayerChairId(secondChairId);
//            int thirdChairIdStatus = home.getPlayerByChairId(thirdChairId).getOperatorStatus();
//            int fourthChairId = nextOperaPlayerChairId(thirdChairId);
//            int fourthChairIdStatus = home.getPlayerByChairId(fourthChairId).getOperatorStatus();
//            String operaModel = stringBuffer.append(home.getInitLordChairIdStatus()).append(secondChairIdStatus).append(thirdChairIdStatus)
//                    .append(fourthChairIdStatus).append(firstChairIdStatus).toString();
        String operaModel = "";
        Home home = null;
        int firstChairIdStatus = 0;
        if (home.getInitLordChairIdStatus() == 1 & firstChairIdStatus == 4) {

        } else if (home.getInitLordChairIdStatus() == 1 & firstChairIdStatus == 3) {

        }
    }

}
