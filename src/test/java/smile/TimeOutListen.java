package smile;

import smile.service.poker.CardPoker;
import smile.tool.PlayerTimer;
import smile.tool.PlayerTimerTask;
import smile.tool.PlayerTimerTaskInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Package: smile
 * @Description: ${todo}
 * @date: 2018/4/20 下午1:16
 * @author: liuxin
 */
public class TimeOutListen {
    public static void main(String[] args) throws Exception {

        PlayerTimer timer = new PlayerTimer();
        TimeOutListen timeOutListen = new TimeOutListen();
        List<PlayerTimerTaskInfo> timerTaskInfoList = new ArrayList<>();
        PlayerTimerTask Task = new PlayerTimerTask() {
            @Override
            public void run() {
                System.out.println("5秒后开始执行");
            }
        };
        timerTaskInfoList.add(new PlayerTimerTaskInfo(false, 5000, Task));
        timeOutListen.timeOutListen2(timerTaskInfoList, timer);
        PlayerTimerTaskInfo playerTimerTask = timerTaskInfoList.get(0);
        playerTimerTask.cancel();
        PlayerTimerTask Task2 = new PlayerTimerTask() {
            @Override
            public void run() {
                System.out.println("10秒后开始执行");
            }
        };
        timerTaskInfoList.remove(0);
        timerTaskInfoList.add(new PlayerTimerTaskInfo(false, 5000, Task2));

    }


    public void timeOutListen2(List<PlayerTimerTaskInfo> playerTimerTaskInfos, PlayerTimer timer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    PlayerTimerTaskInfo timerTaskInfo = playerTimerTaskInfos.get(0);
                    long delay = timerTaskInfo.getDelay();
                    PlayerTimerTask timerTask = timerTaskInfo.getTimerTask();
                    if (!timerTaskInfo.isExe()) {
                        timer.schedule(timerTask, delay);
                        timerTaskInfo.setExe(true);
                    }
                }
            }
        }).start();
    }


}

