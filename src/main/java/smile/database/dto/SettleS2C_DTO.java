package smile.database.dto;

import lombok.Data;
import smile.protocol.Datagram;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: liuxin
 * @date: 2018/4/4 上午12:16
 */
@Data
public class SettleS2C_DTO implements Datagram{
    /**
     * 胜出玩家
     */
    private String charid;
    /**
     * 剩余局数: 房主的信息
     */
    private String cardNum;

    private List<SettleDTO> userSettles;

    /**
     * 抵住
     */
    private String blind;
    /**
     * 房间总局数
     */
    private String roomNum;
    /**
     * 剩余牌局数
     */
    private String currentRoomNum;

    public class SettleDTO{
        private String uid;
        private String grade;
        private List<String> pokers;


        public SettleDTO(String uid, String grade) {
            this.uid = uid;
            this.grade = grade;
            this.pokers=new ArrayList<>(1);
        }

        public SettleDTO(String uid, String grade,List<String>pokers) {
            this.uid = uid;
            this.grade = grade;
            this.pokers=pokers;
        }

        @Override
        public String toString() {
            return "SettleDTO{" +
                    "uid='" + uid + '\'' +
                    ", grade='" + grade + '\'' +
                    ", pokers=" + pokers +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SettleS2C_DTO{" +
                "charid='" + charid + '\'' +
                ", cardNum='" + cardNum + '\'' +
                ", userSettles=" + userSettles +
                ", blind='" + blind + '\'' +
                ", roomNum='" + roomNum + '\'' +
                ", currentRoomNum='" + currentRoomNum + '\'' +
                '}';
    }
}

