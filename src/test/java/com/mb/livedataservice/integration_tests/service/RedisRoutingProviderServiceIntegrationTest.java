package com.mb.livedataservice.integration_tests.service;

import com.mb.livedataservice.config.redis.RedisRoutingProvider;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.service.RedisRoutingProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestcontainersConfiguration.class)
@ContextConfiguration(initializers = TestcontainersConfiguration.Initializer.class)
class RedisRoutingProviderServiceIntegrationTest {

    @Autowired
    private RedisRoutingProviderService cacheService;

    @Autowired
    private RedisRoutingProvider redisRoutingProvider;

    @Autowired
    private RedisTemplate<String, Object> primaryRedisTemplate;

    @Test
    void cacheData_ShouldRouteAndStoreDataInCorrectDynamicRedisHost_WhenMultipleClustersAreConfigured() {
        // Arrange
        String clusterX = "x";
        String clusterY = "y";
        String keyX = "user:100";
        String valueX = "Alice";
        String keyY = "user:200";
        String valueY = "Bob";

        // Act
        cacheService.cacheData(clusterX, keyX, valueX);
        cacheService.cacheData(clusterY, keyY, valueY);

        // Assertions
        RedisTemplate<String, Object> templateX = redisRoutingProvider.getTemplateForCluster(clusterX);
        RedisTemplate<String, Object> templateY = redisRoutingProvider.getTemplateForCluster(clusterY);

        assertThat(templateX.opsForValue().get(keyX)).isEqualTo(valueX);
        assertThat(templateX.opsForValue().get(keyY)).isNull();
        assertThat(templateY.opsForValue().get(keyY)).isEqualTo(valueY);
        assertThat(templateY.opsForValue().get(keyX)).isNotNull();
        assertThat(primaryRedisTemplate.opsForValue().get(keyX)).isEqualTo(valueX);
    }

    @Test
    void cacheData_ShouldRouteAndStoreListCorrectly_WhenValueIsAList() {
        // Arrange
        String clusterX = "x";
        String clusterY = "y";
        String keyX = "list:clusterX";
        String keyY = "list:clusterY";
        List<String> listX = List.of("Log1", "Log2");
        List<String> listY = List.of("Log3");

        // Act
        cacheService.cacheData(clusterX, keyX, listX);
        cacheService.cacheData(clusterY, keyY, listY);

        // Assertions
        RedisTemplate<String, Object> templateX = redisRoutingProvider.getTemplateForCluster(clusterX);
        RedisTemplate<String, Object> templateY = redisRoutingProvider.getTemplateForCluster(clusterY);

        Object resultX = templateX.opsForValue().get(keyX);
        assertThat(resultX).isEqualTo(listX);
        assertThat(templateX.opsForValue().get(keyY)).isNull();

        Object resultY = templateY.opsForValue().get(keyY);
        assertThat(resultY).isEqualTo(listY);
    }

    @Test
    void cacheData_ShouldRouteAndStoreMapCorrectly_WhenValueIsAMap() {
        // Arrange
        String clusterX = "x";
        String clusterY = "y";
        String keyX = "map:clusterX";
        String keyY = "map:clusterY";
        Map<String, String> mapX = Map.of("theme", "dark", "language", "en");
        Map<String, String> mapY = Map.of("theme", "light");

        // Act
        cacheService.cacheData(clusterX, keyX, mapX);
        cacheService.cacheData(clusterY, keyY, mapY);

        // Assertions
        RedisTemplate<String, Object> templateX = redisRoutingProvider.getTemplateForCluster(clusterX);
        RedisTemplate<String, Object> templateY = redisRoutingProvider.getTemplateForCluster(clusterY);

        Object resultX = templateX.opsForValue().get(keyX);
        assertThat(resultX).isEqualTo(mapX);
        assertThat(templateX.opsForValue().get(keyY)).isNull();

        Object resultY = templateY.opsForValue().get(keyY);
        assertThat(resultY).isEqualTo(mapY);
    }

    @Test
    void cacheData_ShouldRouteAndStoreSetCorrectly_WhenValueIsASet() {
        // Arrange
        String clusterX = "x";
        String clusterY = "y";
        String keyX = "set:clusterX";
        String keyY = "set:clusterY";
        Set<String> setX = Set.of("ADMIN", "USER");
        Set<String> setY = Set.of("GUEST");

        List<String> expectedListX = List.of("ADMIN", "USER");
        List<String> expectedListY = List.of("GUEST");

        // Act
        cacheService.cacheData(clusterX, keyX, setX);
        cacheService.cacheData(clusterY, keyY, setY);

        // Assertions
        RedisTemplate<String, Object> templateX = redisRoutingProvider.getTemplateForCluster(clusterX);
        RedisTemplate<String, Object> templateY = redisRoutingProvider.getTemplateForCluster(clusterY);

        Object resultX = templateX.opsForValue().get(keyX);
        assertThat(resultX).isInstanceOf(List.class);
        assertThat((List<String>) resultX).containsExactlyInAnyOrderElementsOf(expectedListX);
        assertThat(templateX.opsForValue().get(keyY)).isNull();

        Object resultY = templateY.opsForValue().get(keyY);
        assertThat(resultY).isInstanceOf(List.class);
        assertThat((List<String>) resultY).containsExactlyInAnyOrderElementsOf(expectedListY);
    }
}
