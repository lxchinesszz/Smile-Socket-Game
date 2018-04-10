package smile;


import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Package: smile
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/27 下午2:12
 */
public class Main {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getContextClassLoader().getParent().getParent());
        IdentityHashMap m = new IdentityHashMap();
        /**
         * 两个地址值,不相等,所以保存了两个
         */
        String key = new String("key_1");
        String key0 = new String("key_1");
        m.put(key, "value1");
        m.put(key0, "value1");

        /**
         * 因为两个key一样,是在字符串常量池中,所以地址是相同的.所以就保存最后一个
         */
        m.put("key_2", "value_2");
        m.put("key_2", "value_2_2");

        //{key_2=value_2_2, key_1=value1, key_1=value1}
        System.out.println(m);

        Object key_2 = m.get("key_2");


        String s="welect ok";
        s.replace("e","E");
        System.out.println(s);
        System.out.println(s.replace("e","E"));


    }
}
