package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.data.model.redis.RedisHashData;
import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.service.RedisHashService;
import com.mb.livedataservice.util.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedisController {

    private final RedisHashService redisHashService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Create RedisHashData
     */
    @PostMapping("/redis-hash")
    public RedisHashData createRedisHashData() {
        log.info("Received a request to create RedisHashData. createRedisHashData.");
        return redisHashService.save(RedisHashData.builder().destination("hello_world").build());
    }

    /**
     * Publish order to Redis
     */
    @PostMapping("/orders")
    public void publish(@RequestBody Order order) {
        stringRedisTemplate.convertAndSend(RedisConstants.ORDERS_TOPIC, objectMapper.writeValueAsString(order));
    }
}
