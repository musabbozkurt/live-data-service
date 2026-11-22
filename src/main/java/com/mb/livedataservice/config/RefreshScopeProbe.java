package com.mb.livedataservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Refresh-scoped bean that verifies {@link RefreshScope} works alongside the default {@code process-aot} build.
 */
@Getter
@Component
@RefreshScope
public class RefreshScopeProbe {

    @Value("${refresh-probe.message:initial}")
    private String message;
}
