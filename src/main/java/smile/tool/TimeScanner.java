package smile.tool;

import com.google.common.util.concurrent.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Package: smile.tool
 * @Description: ${todo}
 * @author: liuxin
 * @date: 2018/3/29 下午8:17
 */
public class TimeScanner {
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        ListeningScheduledExecutorService listeningScheduledExecutor = MoreExecutors.listeningDecorator(scheduledExecutorService);

        ListenableScheduledFuture<Boolean> schedule = listeningScheduledExecutor.schedule(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                //TODO 判断用户当前状态是否 为正常,当收到操作的响应,就立即关闭线程
                System.out.println("你好");
                return true;
            }
        }, 4, TimeUnit.SECONDS);


        Futures.addCallback(schedule, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result){
                    //当成功,收到返回,就获取下一个人的,
                    System.out.println(result);
                    System.out.println("关闭线程池");
                    listeningScheduledExecutor.shutdown();
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        },listeningScheduledExecutor);
        /**
         * 我就去判断地主是否抢地主,15s,后如何地主没有叫地主.
         */

        schedule.cancel(true);
        System.out.println("1111111");


        ListenableScheduledFuture<Boolean> schedule1 = listeningScheduledExecutor.schedule(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                //TODO 判断用户当前状态是否 为正常,当收到操作的响应,就立即关闭线程
                System.out.println("你好3");
                return true;
            }
        }, 3, TimeUnit.SECONDS);


        Futures.addCallback(schedule1, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result){
                    //当成功,收到返回,就获取下一个人的,
                    System.out.println(result);
                    System.out.println("关闭线程池3");
                    listeningScheduledExecutor.shutdown();
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        },listeningScheduledExecutor);
    }
}
