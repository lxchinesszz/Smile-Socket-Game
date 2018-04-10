package smile.tool;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.print.attribute.standard.NumberUp;
import java.util.ArrayList;
import java.util.List;

/**
 * @Package: smile.tool
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/19 下午11:21
 */
public class ListTools {
    public static List toList(Number[] bytes) {
        ArrayList list = Lists.newArrayList();
        for (int i = 0; i < bytes.length; i++) {
            list.add(bytes[i]);
        }
        return list;
    }

    public static List toList(int[] ints) {
        ArrayList list = Lists.newArrayList();
        for (int i = 0; i < ints.length; i++) {
            list.add(ints[i]);
        }
        return list;
    }

    public static List toList(byte[] bytes) {
        ArrayList list = Lists.newArrayList();
        for (int i = 0; i < bytes.length; i++) {
            list.add(bytes[i]);
        }
        return list;
    }


}
