package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.data.redis.model.RedisHashData;
import com.mb.livedataservice.service.RedisHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RedisController {

    private final RedisHashService redisHashService;

    /**
     * Create RedisHashData
     */
    @PostMapping("/redis-hash")
    public RedisHashData createRedisHashData() {
        log.info("Received a request to create RedisHashData. createRedisHashData.");
        return redisHashService.save(RedisHashData.builder().destination("hello_world").build());
    }
}