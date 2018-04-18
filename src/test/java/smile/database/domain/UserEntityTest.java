package smile.database.domain;

import io.netty.channel.SimpleChannelInboundHandler;
import junit.framework.TestCase;
import org.smileframework.tool.serialization.SerializationTools;
import smile.protocol.impl.UserDatagram;
import smile.service.home.Player;
import smile.tool.ListTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Package: smile.database.domain
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/19 下午10:59
 */
public class UserEntityTest extends TestCase {

    public static void main(String[] args) {
//        List<Integer> integers = Arrays.asList(26, 6, 49, 48, 49, 48, 49, 48, 34, 3, 231, 148, 183, 42, 9, 231, 140, 156, 231, 140, 156, 231, 156, 139, 50, 13, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 58, 9, 49, 49, 49, 49, 49, 49, 49, 49, 49);
//        byte[] bytes = SerializationTools.CToJavaByte(integers);
//        for (int i = 0; i < bytes.length; i++) {
//            System.out.println(bytes[i]);
//        }
//
//        UserEntity userEntity = SerializationTools.deserialize(bytes, UserEntity.class);
//        System.out.println(userEntity);

        System.out.println(0-34);
        UserDatagram userDatagram = new UserDatagram();
        userDatagram.setAccessToken("accessToken");
        userDatagram.setGender("男");
        userDatagram.setName("名字");
        userDatagram.setIconurl("icon");
        userDatagram.setUid("uid");
        byte[] serialize = SerializationTools.serialize(userDatagram);
        int[] ints = SerializationTools.JavaToCByte(serialize);
        System.out.println("长度: " + ints.length);
        ListTools.toList(ints).stream().forEach(x -> System.out.println(x));


        List<UserDatagram> list = new ArrayList<>();
        UserDatagram a = new UserDatagram();
        a.setAccessToken("1");

        UserDatagram b = new UserDatagram();
        b.setAccessToken("2");

        UserDatagram c = new UserDatagram();
        c.setAccessToken("3");
        list.add(a);
        list.add(b);
        list.add(c);


        for (int i = 0; i <list.size() ; i++) {
            UserDatagram userDatagram1 = list.get(i);
            if (userDatagram1.getAccessToken().equalsIgnoreCase("3")) {
                list.get(i).setAccessToken("4");
                break;
            }
        }


    }
}
