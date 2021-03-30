package cn.zhouruikang.config;

import cn.zhouruikang.cache.LevelCacheManager;
import cn.zhouruikang.message.ChannelTopicMessageListener;
import cn.zhouruikang.message.MessageService;
import cn.zhouruikang.message.MessageTask;
import cn.zhouruikang.properties.LevelCacheProperties;
import cn.zhouruikang.util.AccessFlushRedisCacheWriter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({LevelCacheProperties.class})
public class LevelCacheAutoConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        redisTemplate.setConnectionFactory(connectionFactory);

        // 使用 String 序列化方式，序列化 KEY 。
        redisTemplate.setKeySerializer(RedisSerializer.string());

        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE。
        redisTemplate.setValueSerializer(RedisSerializer.json());
        return redisTemplate;
    }

    @Bean
    public RedisCacheWriter redisCacheWrite(RedisConnectionFactory connectionFactory, LevelCacheProperties levelCacheProperties) {
        return new AccessFlushRedisCacheWriter(connectionFactory, Duration.ofSeconds(Long.valueOf(levelCacheProperties.getPreLoadTime())), Duration.ofMillis(50L));
    }


    @Bean
    public LevelCacheManager levelCacheManager(RedisTemplate redisTemplate, RedisCacheWriter redisCacheWriter, LevelCacheProperties levelCacheProperties) {
        return new LevelCacheManager(redisTemplate, redisCacheWriter, levelCacheProperties);
    }

    @Bean
    public MessageService messageService(RedisTemplate redisTemplate, LevelCacheManager levelCacheManager, LevelCacheProperties levelCacheProperties) {
        MessageService messageService = new MessageService(redisTemplate, levelCacheManager, levelCacheProperties);
        messageService.init();
        return messageService;
    }

    @Bean
    public MessageTask messageTask(MessageService messageService) {
        return new MessageTask(messageService);
    }

    @Bean
    public RedisMessageListenerContainer listenerContainer(RedisConnectionFactory connectionFactory, MessageService messageService, LevelCacheProperties levelCacheProperties) {
        // 创建 RedisMessageListenerContainer 对象
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        container.setConnectionFactory(connectionFactory);

        // 添加监听器
        container.addMessageListener(new ChannelTopicMessageListener(messageService), new ChannelTopic(levelCacheProperties.getChannelTopic()));
        return container;
    }
}
