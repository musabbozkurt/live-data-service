package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.queue.dto.consumer.Order;
import com.mb.livedataservice.util.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.annotation.RedisListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderRedisListener {

    @RedisListener(topic = RedisConstants.ORDERS_TOPIC)
    public void handleMessage(Order order) {
        log.info("Received order: {}", order);
    }
}
