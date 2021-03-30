package cn.zhouruikang.cache;

import cn.zhouruikang.message.Message;
import cn.zhouruikang.message.MessageType;
import cn.zhouruikang.properties.LevelCacheProperties;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class LevelCache extends AbstractValueAdaptingCache {
    private static final Logger logger = LoggerFactory.getLogger(LevelCache.class);

    private String name;

    private EhCacheCache ehCacheCache;

    private RedisCache redisCache;

    private RedisTemplate redisTemplate;

    private LevelCacheProperties levelCacheProperties;


    public LevelCache(String name, EhCacheCache ehCacheCache, RedisCache redisCache, RedisTemplate redisTemplate,LevelCacheProperties levelCacheProperties) {
        super(true);
        this.name = name;
        this.ehCacheCache = ehCacheCache;
        this.redisCache = redisCache;
        this.redisTemplate = redisTemplate;
        this.levelCacheProperties = levelCacheProperties;;
    }

    @Override
    protected Object lookup(Object o) {
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public LevelCache getNativeCache() {
        return this;
    }


    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        Element element = ehCacheCache.getNativeCache().get(key);
        if (element != null) {
            logger.debug("一级缓存 get 命中: key = {}, value = {}", key, element.getObjectValue());
            return toValueWrapper(element.getObjectValue());
        }

        ValueWrapper value = redisCache.get(key);
        if (value != null) {
            ehCacheCache.putIfAbsent(key, value.get());
        }
        return value;
    }

    @Override
    @Nullable
    public <T> T get(Object key, Class<T> type) {
        Element element = ehCacheCache.getNativeCache().get(key);
        Object value = element != null ? fromStoreValue(element.getObjectValue()) : null;
        if (value != null && type != null && type.isInstance(value)) {
            logger.debug("一级缓存 get 命中: key = {}, value = {}", key, value);
            return (T) value;
        }

        value = redisCache.get(key, type);
        if (value != null) {
            ehCacheCache.putIfAbsent(key, value);
        }
        return (T) value;
    }


    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        Element element = ehCacheCache.getNativeCache().get(key);
        if (element != null) {
            logger.debug("一级缓存 get 命中: key = {}, value = {}", key, element.getObjectValue());
            return (T) element.getObjectValue();
        }

        T value = redisCache.get(key, valueLoader);
        ehCacheCache.putIfAbsent(key, value);
        return value;
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        redisCache.put(key, value);
        logger.debug("二级缓存 put: key = {}, value = {} 完成", key, value);

        pushMessageToDeleteFirstCache(MessageType.EVICT, this.name, key);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {

        ValueWrapper result = redisCache.putIfAbsent(key, value);
        logger.debug("二级缓存 putIfAbsent: key = {}, value = {} 完成", key, value);

        pushMessageToDeleteFirstCache(MessageType.EVICT, this.name, key);
        return result;
    }

    @Override
    public void evict(Object key) {
        redisCache.evict(key);
        logger.debug("二级缓存 evict: key = {} 完成", key);

        pushMessageToDeleteFirstCache(MessageType.EVICT, this.name, key);
    }


    @Override
    public void clear() {
        // 删除的时候要先删除二级缓存再删除一级缓存，否则有并发问题
        redisCache.clear();
        logger.debug("二级缓存 clear: cacheName = {} 完成", name);
        pushMessageToDeleteFirstCache(MessageType.CLEAR, this.name, null);
    }


    public EhCacheCache getFirstCache(){
        return ehCacheCache;
    }


    private void pushMessageToDeleteFirstCache(MessageType messageType, String name, Object key) {

        Message message = new Message().setMessageType(messageType);
        switch (messageType) {
            case EVICT:
                message.setCacheName(name).setKey(key);
                break;
            case CLEAR:
                message.setCacheName(name);
                break;
            default:
                break;
        }

        redisTemplate.opsForList().leftPush(levelCacheProperties.getMessageListKey(), message);
        redisTemplate.expire(levelCacheProperties.getMessageListKey(), 25, TimeUnit.HOURS);
        redisTemplate.convertAndSend(levelCacheProperties.getChannelTopic(), "time-to-pull-messages");
        logger.debug("push消息至 messageList: {}, 同时向频道发送删除一级缓存的提示消息", levelCacheProperties.getMessageListKey());
    }
}
