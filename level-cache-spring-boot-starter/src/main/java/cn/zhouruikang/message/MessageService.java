package cn.zhouruikang.message;

import cn.zhouruikang.cache.LevelCache;
import cn.zhouruikang.cache.LevelCacheManager;
import cn.zhouruikang.properties.LevelCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private static final AtomicLong OFFSET = new AtomicLong(-1);

    RedisTemplate redisTemplate;

    LevelCacheManager levelCacheManager;

    LevelCacheProperties levelCacheProperties;

    public MessageService(RedisTemplate redisTemplate, LevelCacheManager levelCacheManager, LevelCacheProperties levelCacheProperties) {
        this.redisTemplate = redisTemplate;
        this.levelCacheManager = levelCacheManager;
        this.levelCacheProperties = levelCacheProperties;
    }

    public void init() {
        long curOffset = redisTemplate.opsForList().size(levelCacheProperties.getMessageListKey()) - 1;
        if (curOffset < 0) {
            return;
        }
        OFFSET.getAndSet(curOffset > 0 ? curOffset : 0);
    }


    public void pullMessages() {
        long curOffset = redisTemplate.opsForList().size(levelCacheProperties.getMessageListKey()) - 1;
        // 没有消息
        if (curOffset < 0) {
            logger.debug("messageList无消息");
            return;
        }
        // 更新本地消息偏移量
        long preOffset = OFFSET.getAndSet(curOffset > 0 ? curOffset : 0);
        if (preOffset >= curOffset) {
            logger.debug("messageList无最新消息: preOffset = {}, curOffset = {}", preOffset, curOffset);
            return;
        }
        List<Object> messages = redisTemplate.opsForList().range(levelCacheProperties.getMessageListKey(), 0, curOffset - preOffset - 1);

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        logger.debug("拉取到{}条消息", messages.size());

        for (Object message : messages) {

            if (StringUtils.isEmpty(message)) {
                continue;
            }

            Message messageObject = (Message) message;

            LevelCache cache = (LevelCache) levelCacheManager.getCache(messageObject.getCacheName());


            switch (messageObject.getMessageType()) {
                case EVICT:
                    cache.getFirstCache().evict(messageObject.getKey());
                    logger.debug("evict一级缓存单key: key = {}", messageObject.getKey());
                    break;
                case CLEAR:
                    cache.getFirstCache().clear();
                    logger.debug("clear一级缓存全部key: cacheName = {}", cache.getName());
                    break;
                default:
                    break;
            }

        }

    }


    public void clearMessageList() {
        redisTemplate.delete(levelCacheProperties.getMessageListKey());
        // 重置偏移量
        OFFSET.getAndSet(-1);
    }


}
