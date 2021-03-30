package cn.zhouruikang.cache;

import cn.zhouruikang.properties.LevelCacheProperties;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.*;

public class LevelCacheManager extends AbstractTransactionSupportingCacheManager {

    RedisTemplate redisTemplate;

    RedisCacheWriter redisCacheWriter;

    LevelCacheProperties levelCacheProperties;

    EhCacheCacheManager ehCacheCacheManager;

    RedisCacheManager redisCacheManager;


    public LevelCacheManager() {
    }

    public LevelCacheManager(RedisTemplate redisTemplate, RedisCacheWriter redisCacheWriter, LevelCacheProperties levelCacheProperties) {
        this.redisTemplate = redisTemplate;
        this.redisCacheWriter = redisCacheWriter;
        this.levelCacheProperties = levelCacheProperties;
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        //初始化 ehCacheCacheManager
        //注意: 从ehcache.xml获取配置信息只能通过EhCacheManagerUtils.buildCacheManager()，故这里创建ehCacheCacheManager是必要的
        CacheManager ehCacheManager = EhCacheManagerUtils.buildCacheManager();
        Assert.state(ehCacheManager != null, "No ehCacheManager set");
        Status status = ehCacheManager.getStatus();
        if (!Status.STATUS_ALIVE.equals(status)) {
            throw new IllegalStateException("An 'alive' EhCache CacheManager is required - current cache is " + status.toString());
        }
        ehCacheCacheManager = new EhCacheCacheManager(ehCacheManager);

        //初始化 redisCacheManager
        //以ehcache配置的各缓存名为主，若levelCacheProperties中redis有该缓存名，则按其配置的过期时间，否则按配置的默认过期时间
        //注意: redisCache的构造方法为protected,故只能通过本包的redisCacheManager来创建，所以这里创建redisCacheManager是必要的
        RedisCacheConfiguration defaultRedisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(Long.valueOf(levelCacheProperties.getDefaultExpire())));
        Map<String, String> cachesExpire = levelCacheProperties.getCachesExpire();
        Map<String, RedisCacheConfiguration> initialCacheConfiguration = new LinkedHashMap<>();

        Iterator var1 = cachesExpire.entrySet().iterator();
        while(var1.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry) var1.next();
            String name = entry.getKey();
            String cacheExpire = entry.getValue();
            initialCacheConfiguration.put(name, defaultRedisCacheConfig.entryTtl(Duration.ofSeconds(Long.valueOf(cacheExpire))));
        }
        redisCacheManager = new AccessFlushRedisCacheManager(redisCacheWriter, defaultRedisCacheConfig, initialCacheConfiguration);
        redisCacheManager.initializeCaches();

        //组合生成
        String[] names = ehCacheCacheManager.getCacheManager().getCacheNames();
        Collection<Cache> caches = new LinkedHashSet(names.length);

        for (String name: names) {
            caches.add(new LevelCache(name, new EhCacheCache(ehCacheManager.getEhcache(name)), (RedisCache) redisCacheManager.getCache(name), redisTemplate, levelCacheProperties));
        }
        return caches;
    }






}
