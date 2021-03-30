package cn.zhouruikang.cache;

import cn.zhouruikang.util.AccessFlushRedisCacheWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

public class AccessFlushRedisCache extends RedisCache {
    private static final Logger logger = LoggerFactory.getLogger(AccessFlushRedisCache.class);

    AccessFlushRedisCacheWriter cacheWriter;

    protected AccessFlushRedisCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig) {
        super(name, cacheWriter, cacheConfig);
        this.cacheWriter = (AccessFlushRedisCacheWriter) cacheWriter;
    }

    protected Object lookup(Object key) {
        String name = getName();
        byte[] binKey = this.serializeCacheKey(this.createCacheKey(key));

        byte[] value = cacheWriter.get(name, binKey);
        if (value == null) return null;

        logger.debug("二级缓存 get 命中: key = {}, value = {}", key, this.deserializeCacheValue(value));

        boolean needFlush = cacheWriter.needFlush(name, binKey);
        if (needFlush){
            cacheWriter.pExpire(name, binKey, getCacheConfiguration().getTtl());
            logger.debug("二级缓存 主动刷新缓存时间: key = {}", key);
        }
        return this.deserializeCacheValue(value);
    }
}
