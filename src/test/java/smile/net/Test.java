package smile.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.smileframework.tool.date.StopWatch;
import smile.database.domain.UserEntity;
import smile.service.home.Home;
import smile.service.home.HomeInfo;
import smile.tool.GameHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Package: smile.net
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/4/9 下午10:01
 */
public class Test {
    public static void main(String[] args) {
      int []arr=new int[]{1,2,3,4,5,6,34534,5,6546,776567,8768,9879,12,343,54,65,7,5,7,8,9,4,34,2,2,1,3,4,4,3,54};
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        System.out.println(erfen(arr,5));
        stopWatch.stop();
//        Collections.binarySearch()
        String s = stopWatch.prettyPrint();
        System.out.println(s);
    }
    public static int erfen(int[]arr,int key){
        int l=0;
        int h=arr.length-1;
        int mid=(l+h)>>1;
        if (l<h){
            if (arr[mid]>key){

            }
        }
        return 0;
    }


}
