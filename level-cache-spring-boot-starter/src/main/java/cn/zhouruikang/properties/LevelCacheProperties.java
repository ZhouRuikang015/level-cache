package cn.zhouruikang.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "levelcache")
public class LevelCacheProperties {
    private final String CHANNEL_TOPIC_PRIFIX = "level-cache-channel-topic:%s";

    private final String MESSAGE_LIST_KEY_PRIFIX = "level-cache-message-list-key:%s";

    @Value("${spring.application.name}")
    private String namespace ;

    private String defaultExpire = "60";

    private Map<String, String> cachesExpire = new HashMap<>();

    private String preLoadTime = "20";

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMessageListKey() {
        return String.format(MESSAGE_LIST_KEY_PRIFIX, namespace);
    }

    public String getChannelTopic() {
        return String.format(CHANNEL_TOPIC_PRIFIX, namespace);
    }



    public String getDefaultExpire() {
        return defaultExpire;
    }

    public void setDefaultExpire(String defaultExpire) {
        this.defaultExpire = defaultExpire;
    }

    public Map<String, String> getCachesExpire() {
        return cachesExpire;
    }

    public void setCachesExpire(Map<String, String> cachesExpire) {
        this.cachesExpire = cachesExpire;
    }

    public String getPreLoadTime() {
        return preLoadTime;
    }

    public void setPreLoadTime(String preLoadTime) {
        this.preLoadTime = preLoadTime;
    }
}
