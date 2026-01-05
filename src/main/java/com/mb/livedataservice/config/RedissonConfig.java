package com.mb.livedataservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    @ConditionalOnProperty(value = "redisson.enabled", havingValue = "true")
    public RedissonClient redissonClient(@Value("${redisson.url}") String address) {
        Config config = new Config();
        config.useSingleServer().setAddress(address);

        return Redisson.create(config);
    }
}
