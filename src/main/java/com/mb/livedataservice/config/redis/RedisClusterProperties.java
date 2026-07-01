package com.mb.livedataservice.config.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisClusterProperties {

    private Map<String, ClusterConfig> clusters = new HashMap<>();

    @Setter
    @Getter
    public static class ClusterConfig {
        private String host;
        private String password;
        private int port;
        private boolean primary;
    }
}
