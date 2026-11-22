package com.mb.livedataservice.integration_tests.config;

import com.mb.livedataservice.config.RefreshScopeProbe;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RefreshScopeProbe.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
class RefreshScopeProbeIntegrationTest {

    @Autowired
    private RefreshScopeProbe refreshScopeProbe;

    @Autowired
    private RefreshScope refreshScope;

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void refreshAll_ShouldRebindRefreshScopedBean_WhenPropertyChanges() {
        // Arrange
        assertTrue(AopUtils.isAopProxy(refreshScopeProbe));
        assertEquals("initial", refreshScopeProbe.getMessage());
        TestPropertyValues.of("refresh-probe.message=refreshed").applyTo(environment);

        // Act
        refreshScope.refreshAll();

        // Assertions
        assertEquals("refreshed", refreshScopeProbe.getMessage());
    }
}
