package smile.tool;
import java.util.HashMap;
import java.util.Map;

/**
 * @Package: smile.tool
 * @Description: 断线重连工具
 * @author: liuxin
 * @date: 2018/4/5 下午4:33
 */
public class BreakConnectTools {
    private static final Map<String,String> breakUidAndHidAsMap=new HashMap<>();

    public static void addUidAndHid(String uid,String hid){
        breakUidAndHidAsMap.put(uid,hid);
    }

    public static String getHidByUid(String uid){
        return breakUidAndHidAsMap.getOrDefault(uid,"");
    }

    public static void clearUid(String uid){
        breakUidAndHidAsMap.remove(uid);
    }
}
