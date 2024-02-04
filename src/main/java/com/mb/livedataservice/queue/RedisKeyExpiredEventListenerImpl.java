package com.mb.livedataservice.queue;

import com.mb.livedataservice.data.redis.model.RedisHashData;
import com.mb.livedataservice.service.RedisHashService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class RedisKeyExpiredEventListenerImpl {

    private final RedisHashService redisHashService;

    @EventListener(condition = "#event.keyspace == 'RedisHashData'")
    public void redisExpiredKeyEventForRedisHashData(RedisKeyExpiredEvent<?> event) {
        log.info("Redis key expired event log. RedisHashData - event:{}", event.toString());
        redisHashService.delete(RedisHashData.builder().id(new String(event.getId())).build());
    }
}
