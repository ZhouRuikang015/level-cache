package cn.zhouruikang.message;

import cn.zhouruikang.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageTask implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MessageTask.class);

    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("lever-cache-message-task-"));

    private MessageService messageService;

    public MessageTask(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        pullMessagesTask();
        clearMessageListTask();
    }

    private void pullMessagesTask() {
        logger.info("任务pullMessagesTask初始启动, 定时任务每隔30秒执行pullMessages，被动从messageList拉取消息");

        executor.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("定时任务执行pullMessages，被动从messageList拉取消息");
                messageService.pullMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  初始时间间隔是30秒
        }, 5, 30, TimeUnit.SECONDS);
    }


    private void clearMessageListTask() {
        logger.info("任务pullMessagesTask初始启动, 每天晚上凌晨3:00清空messageList");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long initialDelay = System.currentTimeMillis() - cal.getTimeInMillis();
        initialDelay = initialDelay > 0 ? initialDelay : 0;

        // 每天晚上凌晨3:00执行任务
        executor.scheduleWithFixedDelay(() -> {
            try {
                logger.info("定时任务执行clearMessageList, 开始清空messageList");
                messageService.clearMessageList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

}
