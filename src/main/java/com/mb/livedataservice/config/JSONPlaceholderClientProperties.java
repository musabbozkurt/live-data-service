package com.mb.livedataservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "clients.json-placeholder")
public class JSONPlaceholderClientProperties {

    private String url;
    private String clientId;
    private String clientSecret;
}
