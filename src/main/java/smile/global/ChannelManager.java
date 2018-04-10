package smile.global;

import io.netty.channel.Channel;
import org.smileframework.ioc.bean.annotation.SmileComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Package: smile.global
 * @Description: 连接管理器
 * @author: liuxin
 * @date: 2018/3/27 下午4:21
 */
@SmileComponent
public class ChannelManager {

    private int maxCount = 30000;

    /**
     * key  :	role_id / sessionId
     * value:	Channel
     */
    private final ConcurrentMap<String, Channel> sessions = new ConcurrentHashMap<>();

    /**
     * ip blacklist
     */
    private Set<String> blacklist = new HashSet<>();

    public ChannelManager() {

    }

    public void addChannel(String key, Channel channel) {
        sessions.put(key, channel);
    }

    public void removeChannel(String key) {
        sessions.remove(key);
    }

    public Channel getChannel(String key) {
        return sessions.get(key);
    }

    public int getSessionCount() {
        return sessions.size();
    }

    public ConcurrentMap<String, Channel> getSessions() {
        return sessions;
    }

    public Collection<String> getOnlineRoleIds() {
        return sessions.keySet();
    }

    public boolean isFull() {
        return getSessionCount() >= maxCount;
    }

    public boolean isBlackIp(String ip) {
        return blacklist.contains(ip);
    }
}
