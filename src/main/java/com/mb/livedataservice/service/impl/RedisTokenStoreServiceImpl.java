package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.service.RedisTokenStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "services.token.store", havingValue = "redis")
public class RedisTokenStoreServiceImpl implements RedisTokenStoreService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    // tokenId should be unique for every integration.
    @Override
    public String getToken(String tokenId, String key) {
        String token = stringRedisTemplate.opsForValue().get(tokenId);
        if (!StringUtils.hasLength(token)) {
            RLock lock = redissonClient.getLock(key);

            boolean control = false;
            while (lock.isLocked()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                    control = true;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            if (control) {
                return stringRedisTemplate.opsForValue().get(tokenId);
            }
            try {
                lock.lock(3, TimeUnit.SECONDS);
                // Call 3rd party to get token
                storeToken(tokenId, token);
            } catch (Exception ex) {
                log.info("Error occurred while getting and saving 3rd party token exception. Exception: {}", ExceptionUtils.getStackTrace(ex));
            } finally {
                if (lock.isLocked()) {
                    lock.unlock();
                }
            }
        }
        return token;
    }

    @Override
    public void storeToken(String tokenId, String key) {
        stringRedisTemplate.opsForValue().set(tokenId, key, Duration.ofMinutes(30));
    }

    @Override
    public void deleteToken(String tokenId, String key) {
        RLock lock = redissonClient.getLock(key);

        boolean control = true;
        while (lock.isLocked()) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
                control = false;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        if (control) {
            stringRedisTemplate.delete(tokenId);
        }
    }
}
