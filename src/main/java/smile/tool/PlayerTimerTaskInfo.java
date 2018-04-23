package smile.tool;

/**
 * @Package: smile
 * @Description:
 * @date: 2018/4/20 下午3:29
 * @author: liuxin
 */
public class PlayerTimerTaskInfo {

    /**
     * 延迟时间
     */
    private long delay;
    /**
     * 是否已经执行
     */
    private boolean isExe;
    /**
     * 任务
     */
    private PlayerTimerTask timerTask;

    public PlayerTimerTaskInfo(boolean isExe,long delay, PlayerTimerTask timerTask) {
        this.isExe = isExe;
        this.delay=delay;
        this.timerTask = timerTask;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void cancel(){
        timerTask.cancel();
    }

    public boolean isExe() {
        return isExe;
    }

    public void setExe(boolean exe) {
        isExe = exe;
    }

    public PlayerTimerTask getTimerTask() {
        return timerTask;
    }

    public void setTimerTask(PlayerTimerTask timerTask) {
        this.timerTask = timerTask;
    }
}
