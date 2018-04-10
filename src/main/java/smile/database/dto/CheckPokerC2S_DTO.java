package smile.database.dto;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Data;
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
 * @date: 2018/4/3 下午1:53
 */
@Data
public class CheckPokerC2S_DTO implements Datagram {
    private String charid;
    private String pokers;
    private String hid;

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
