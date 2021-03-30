package cn.zhouruikang.message;

import cn.zhouruikang.cache.LevelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class ChannelTopicMessageListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(ChannelTopicMessageListener.class);

    MessageService messageService;

    public ChannelTopicMessageListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        logger.debug("收到频道消息，主动执行pullMessages方法，从messageList拉取消息。。");
        messageService.pullMessages();
    }
}
