package smile.database.dto;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Data;
import org.smileframework.tool.string.StringTools;
import smile.protocol.Datagram;
import smile.service.poker.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Package: smile.database.dto
 * @Description:
 * @author: liuxin
 * @date: 2018/3/29 下午9:08
 */
@Data
public class OperatorC2S_DTO implements Datagram {

    private String hid;
    /**
     * 用户操作状态
     */
    private String operationStatus;
    private String pokers;
    /**
     * 座位号
     */
    private String chairId;

    public List<Card> getPokerAsList() {
        if (pokers == null||pokers.equalsIgnoreCase("")) {
            return null;
        }
        Splitter splitter = Splitter.on(",").omitEmptyStrings();
        List<String> strings = splitter.splitToList(this.pokers);
        Stream<Card> cardStream = splitter.splitToList(this.pokers).stream().map(new Function<String, Card>() {
            @Override
            public Card apply(String s) {
                return new Card(Integer.parseInt(s));
            }
        });
        ArrayList<Card> cards = Lists.newArrayList(cardStream.collect(Collectors.toList()));
        return cards;
    }
}
