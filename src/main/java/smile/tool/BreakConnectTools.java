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

    public static void addUidAndHid(String userToken,String hid){
        breakUidAndHidAsMap.put(userToken,hid);
    }

    public static String getHidByUid(String userToken){
        return breakUidAndHidAsMap.getOrDefault(userToken,"");
    }
}
