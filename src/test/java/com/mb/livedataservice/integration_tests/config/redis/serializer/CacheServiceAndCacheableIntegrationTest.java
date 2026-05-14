package com.mb.livedataservice.integration_tests.config.redis.serializer;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import com.mb.livedataservice.integration_tests.config.redis.serializer.CacheableTestService.Template;
import com.mb.livedataservice.integration_tests.config.redis.serializer.CacheableTestService.TemplateDto;
import com.mb.livedataservice.integration_tests.config.redis.serializer.CacheableTestService.TemplateSimpleDto;
import com.mb.livedataservice.service.CacheService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests that verify CacheService and @Cacheable annotation work together.
 * <p>
 * Tests the following cross-compatibility scenarios:
 * <ul>
 *   <li>Store via @Cacheable → read via CacheService</li>
 *   <li>Store via CacheService → read via @Cacheable (cache hit)</li>
 *   <li>Store via CacheService → read via CacheService.getMap with different DTO type</li>
 *   <li>Map without @class in values (CacheService put) read back correctly</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestcontainersConfiguration.class)
@DisplayName("CacheService and @Cacheable Cross-Compatibility Tests")
class CacheServiceAndCacheableIntegrationTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CacheableTestService cacheableTestService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeAll
    void setUp() {
        // Flush ALL keys in the Redis database before each test.
        // This is the only reliable way to guarantee isolation when using a shared
        // Testcontainers Redis instance — @CacheEvict only removes keys it knows about
        // and can leave stale keys from previous test runs (especially with withReuse=true).
        if (redisTemplate.getConnectionFactory() != null) {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .serverCommands()
                    .flushDb();
        }

        // Evict all caches before each test
        cacheableTestService.evictAllTemplates();
        cacheableTestService.evictTemplateByType();
        cacheableTestService.evictSingleTemplate();
        cacheableTestService.evictTemplateByType2();
        cacheableTestService.evictAllTemplateIds();

        // Clean up any manual keys
        Set<String> keys = cacheService.getKeys("test:cross:*");
        if (keys != null && !keys.isEmpty()) {
            cacheService.deleteAll(keys);
        }
    }

    // ==================== @Cacheable stores → CacheService reads ====================

    @Nested
    @DisplayName("@Cacheable stores, CacheService reads")
    class CacheableStoresCacheServiceReads {

        @Test
        @DisplayName("Should read List<Template> stored by @Cacheable via CacheService.get(collectionType, elementType)")
        void get_ShouldReturnTypedList_WhenStoredViaCacheable() {
            // Act — @Cacheable stores the list
            List<Template> cacheableResult = cacheableTestService.findAllActiveTemplates();
            assertNotNull(cacheableResult);
            assertEquals(3, cacheableResult.size());

            // Read via CacheService — key format: service-name:cache-value:cache-key
            Collection<Template> cacheServiceResult = cacheService.get("template-service:allTemplates:all", List.class, Template.class);

            // Assertions
            assertNotNull(cacheServiceResult);
            assertEquals(3, cacheServiceResult.size());

            List<Template> resultList = new ArrayList<>(cacheServiceResult);
            Template template = resultList.getFirst();
            assertEquals(104L, template.getId());
            assertEquals("custom_mail_pdf_template.xlsx", template.getName());
            assertEquals("CUSTOM_MAIL_PDF", template.getType());
            assertTrue(template.getActive());
            assertNotNull(template.getAudit());
            assertEquals("test-user", template.getAudit().getCreatedBy());
        }

        @Test
        @DisplayName("Should read List<Integer> stored by @Cacheable via CacheService.get(collectionType, elementType)")
        void get_ShouldReturnTypedIntegerList_WhenStoredViaCacheable() {
            // Act — @Cacheable stores the list
            List<Integer> cacheableResult = cacheableTestService.findAllActiveTemplateIds();
            assertNotNull(cacheableResult);
            assertEquals(3, cacheableResult.size());

            // Read via CacheService
            Collection<Integer> cacheServiceResult = cacheService.get("template-service:templateIds:all", List.class, Integer.class);

            // Assertions
            assertNotNull(cacheServiceResult);
            assertEquals(3, cacheServiceResult.size());

            List<Integer> resultList = new ArrayList<>(cacheServiceResult);
            assertEquals(104, resultList.get(0));
            assertEquals(130, resultList.get(1));
            assertEquals(47, resultList.get(2));
        }

        @Test
        @DisplayName("Should read List<Template> stored by @Cacheable via CacheService as Set")
        void get_ShouldReturnTypedSet_WhenStoredViaCacheable() {
            // Act — @Cacheable stores the list
            cacheableTestService.findAllActiveTemplates();

            // Read as Set via CacheService
            Set<Template> result = (Set<Template>) cacheService.get("template-service:allTemplates:all", Set.class, Template.class);

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Should read Map<String, TemplateDto> stored by @Cacheable via CacheService.getMap")
        void getMap_ShouldReturnTypedMap_WhenStoredViaCacheable() {
            // Act — @Cacheable stores the map
            Map<String, TemplateDto> cacheableResult = cacheableTestService.getTemplateByType();
            assertNotNull(cacheableResult);
            assertEquals(3, cacheableResult.size());

            // Read via CacheService.getMap
            Map<String, TemplateDto> cacheServiceResult = cacheService.getMap("template-service:templateByType:all", String.class, TemplateDto.class);

            // Assertions
            assertNotNull(cacheServiceResult);
            assertEquals(3, cacheServiceResult.size());
            assertTrue(cacheServiceResult.containsKey("CUSTOM_MAIL_PDF"));
            assertTrue(cacheServiceResult.containsKey("COMPANY_RESEARCH_EXPORT"));
            assertTrue(cacheServiceResult.containsKey("EXCEL_ACTIVITY_STATUS"));

            TemplateDto dto = cacheServiceResult.get("CUSTOM_MAIL_PDF");
            assertEquals(104L, dto.getId());
            assertEquals("custom_mail_pdf_template.xlsx", dto.getName());
            assertTrue(dto.getActive());
        }

        @Test
        @DisplayName("Should read single Template stored by @Cacheable via CacheService.get(class)")
        void get_ShouldReturnTypedObject_WhenStoredViaCacheable() {
            // Act — @Cacheable stores a single object
            Template cacheableResult = cacheableTestService.getTemplateByTypeSingle("CUSTOM_MAIL_PDF");
            assertNotNull(cacheableResult);

            // Read via CacheService
            Template cacheServiceResult = cacheService.get("template-service:singleTemplate:CUSTOM_MAIL_PDF", Template.class);

            // Assertions
            assertNotNull(cacheServiceResult);
            assertEquals(104L, cacheServiceResult.getId());
            assertEquals("custom_mail_pdf_template.xlsx", cacheServiceResult.getName());
            assertEquals("CUSTOM_MAIL_PDF", cacheServiceResult.getType());
            assertNotNull(cacheServiceResult.getAudit());
            assertEquals("test-user", cacheServiceResult.getAudit().getCreatedBy());
        }
    }

    // ==================== CacheService stores → @Cacheable reads ====================

    @Nested
    @DisplayName("CacheService stores, @Cacheable reads")
    class CacheServiceStoresCacheableReads {

        @Test
        @DisplayName("Should read List<Template> stored by CacheService via @Cacheable")
        void findAllActiveTemplates_ShouldReturnTypedList_WhenStoredViaCacheService() {
            // Arrange — store via CacheService using the @Cacheable key format
            List<Template> templates = CacheableTestService.createSampleTemplates();
            cacheService.put("template-service:allTemplates:all", templates);

            // Act — @Cacheable reads (should be a cache hit)
            List<Template> result = cacheableTestService.findAllActiveTemplates();

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());
            Template template = result.getFirst();
            assertEquals(104L, template.getId());
            assertEquals("custom_mail_pdf_template.xlsx", template.getName());
        }

        @Test
        @DisplayName("Should read List<Integer> stored by CacheService via @Cacheable")
        void findAllActiveTemplateIds_ShouldReturnTypedList_WhenStoredViaCacheService() {
            // Arrange — store via CacheService using the @Cacheable key format
            List<Integer> templateIds = CacheableTestService.createSampleTemplateIds();
            cacheService.put("template-service:templateIds:all", templateIds);

            // Act — @Cacheable reads (should be a cache hit)
            List<Integer> result = cacheableTestService.findAllActiveTemplateIds();

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(104, result.get(0));
            assertEquals(130, result.get(1));
            assertEquals(47, result.get(2));
        }

        @Test
        @DisplayName("Should read Map<String, TemplateDto> stored by CacheService via @Cacheable")
        void getTemplateByType_ShouldReturnTypedMap_WhenStoredViaCacheService() {
            // Arrange — store via CacheService
            Map<String, TemplateDto> templateMap = CacheableTestService.createSampleTemplateDtoMap();
            cacheService.put("template-service:templateByType:all", templateMap);

            // Act — @Cacheable reads (should be a cache hit)
            Map<String, TemplateDto> result = cacheableTestService.getTemplateByType();

            // Assertions — verify map structure via @Cacheable
            assertNotNull(result);
            assertEquals(3, result.size());
            assertTrue(result.containsKey("CUSTOM_MAIL_PDF"));
            assertFalse(result.containsKey("@class"));

            // Typed access via CacheService.getMap
            Map<String, TemplateDto> typedResult = cacheService.getMap("template-service:templateByType:all", String.class, TemplateDto.class);
            TemplateDto dto = typedResult.get("CUSTOM_MAIL_PDF");
            assertEquals(104L, dto.getId());
            assertEquals("custom_mail_pdf_template.xlsx", dto.getName());
        }

        @Test
        @DisplayName("Should read single Template stored by CacheService via @Cacheable")
        void getTemplateByTypeSingle_ShouldReturnTypedObject_WhenStoredViaCacheService() {
            // Arrange — store via CacheService
            Template template = CacheableTestService.createSampleTemplates().getFirst();
            cacheService.put("template-service:singleTemplate:CUSTOM_MAIL_PDF", template);

            // Act — @Cacheable reads (should be a cache hit)
            Template result = cacheableTestService.getTemplateByTypeSingle("CUSTOM_MAIL_PDF");

            // Assertions
            assertNotNull(result);
            assertEquals(104L, result.getId());
            assertEquals("custom_mail_pdf_template.xlsx", result.getName());
            assertEquals("CUSTOM_MAIL_PDF", result.getType());
        }
    }

    // ==================== CacheService stores → CacheService reads with different DTO ====================

    @Nested
    @DisplayName("CacheService cross-type reads (map stored as one DTO, read as another)")
    class CacheServiceCrossTypeReads {

        @Test
        @DisplayName("Should store Map<String, TemplateDto> and read as Map<String, TemplateDto> via getMap")
        void getMap_ShouldReturnTypedValues_WhenStoredWithSameValueType() {
            // Arrange
            String key = "test:cross:map:same";
            Map<String, TemplateDto> templateMap = CacheableTestService.createSampleTemplateDtoMap();

            // Act
            cacheService.put(key, templateMap);
            Map<String, TemplateDto> result = cacheService.getMap(key, String.class, TemplateDto.class);

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());
            assertFalse(result.containsKey("@class"));

            TemplateDto dto = result.get("CUSTOM_MAIL_PDF");
            assertNotNull(dto);
            assertEquals(104L, dto.getId());
            assertEquals("custom_mail_pdf_template.xlsx", dto.getName());
            assertEquals("CUSTOM_MAIL_PDF", dto.getType());
            assertTrue(dto.getActive());
            assertEquals(1, dto.getVersion());
        }

        @Test
        @DisplayName("Should store Map<String, TemplateDto> and read values as different compatible DTO")
        void getMap_ShouldReturnConvertedValues_WhenReadWithDifferentCompatibleType() {
            // Arrange — TemplateDto and Template share common fields (id, name, type, path, active, version)
            String key = "test:cross:map:different";
            Map<String, TemplateDto> templateMap = CacheableTestService.createSampleTemplateDtoMap();

            // Act — store as TemplateDto map, read as Template map
            cacheService.put(key, templateMap);
            Map<String, Template> result = cacheService.getMap(key, String.class, Template.class);

            // Assertions — common fields should be mapped correctly
            assertNotNull(result);
            assertEquals(3, result.size());

            Template template = result.get("CUSTOM_MAIL_PDF");
            assertNotNull(template);
            assertEquals(104L, template.getId());
            assertEquals("custom_mail_pdf_template.xlsx", template.getName());
            assertEquals("CUSTOM_MAIL_PDF", template.getType());
            assertTrue(template.getActive());
            // audit is not in TemplateDto, so it should be null
            assertNull(template.getAudit());
        }

        @Test
        @DisplayName("Should store list via CacheService and read as collection with element type")
        void get_ShouldReturnTypedElements_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:list:typed";
            List<Template> templates = CacheableTestService.createSampleTemplates();

            // Act
            cacheService.put(key, templates);
            Collection<Template> result = cacheService.get(key, List.class, Template.class);

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());

            List<Template> resultList = new ArrayList<>(result);
            Template template = resultList.getFirst();
            assertEquals(104L, template.getId());
            assertEquals("custom_mail_pdf_template.xlsx", template.getName());
            assertNotNull(template.getAudit());
            assertEquals("test-user", template.getAudit().getCreatedBy());
        }

        @Test
        @DisplayName("Should store map via CacheService and read first element correctly")
        void getMap_ShouldReturnTypedFirstElement_WhenReadFromMap() {
            // Arrange
            String key = "test:cross:list:first";
            Map<String, TemplateDto> templateMap = CacheableTestService.createSampleTemplateDtoMap();

            // Act
            cacheService.put(key, templateMap);
            Map<String, TemplateDto> result = cacheService.getMap(key, String.class, TemplateDto.class);

            // Assertions — get first value and verify all fields
            assertNotNull(result);
            assertFalse(result.isEmpty());

            TemplateDto firstDto = result.values().stream().toList().getFirst();
            assertNotNull(firstDto);
            assertNotNull(firstDto.getId());
            assertNotNull(firstDto.getName());
            assertNotNull(firstDto.getType());
            assertNotNull(firstDto.getPath());
            assertNotNull(firstDto.getActive());
            assertNotNull(firstDto.getVersion());
        }
    }

    // ==================== No @class in stored data ====================

    @Nested
    @DisplayName("Verify no @class property in stored data")
    class NoClassPropertyTests {

        @Test
        @DisplayName("Map stored via CacheService should not contain @class key")
        void getMap_ShouldNotContainClassKey_WhenStoredViaCacheService() {
            // Arrange
            String key = "test:cross:noclass:map";
            Map<String, TemplateDto> templateMap = CacheableTestService.createSampleTemplateDtoMap();

            // Act
            cacheService.put(key, templateMap);

            // Read raw — CacheService.get returns the raw deserialized object
            // Maps are serialized without @class, so the raw read should be a plain map
            Map<String, TemplateDto> result = cacheService.getMap(key, String.class, TemplateDto.class);

            // Assertions — no @class leaked as a key
            assertNotNull(result);
            assertFalse(result.containsKey("@class"));
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Map stored via @Cacheable should not contain @class key")
        void getMap_ShouldNotContainClassKey_WhenStoredViaCacheable() {
            // Act — store via @Cacheable
            Map<String, TemplateDto> cacheableResult = cacheableTestService.getTemplateByType();
            assertNotNull(cacheableResult);

            // Read via CacheService
            Map<String, TemplateDto> cacheServiceResult = cacheService.getMap("template-service:templateByType:all", String.class, TemplateDto.class);

            // Assertions — no @class leaked as a key
            assertNotNull(cacheServiceResult);
            assertFalse(cacheServiceResult.containsKey("@class"));
            assertEquals(3, cacheServiceResult.size());
        }
    }

    // ==================== Full round-trip scenario ====================

    @Nested
    @DisplayName("Full Round-Trip Scenarios")
    class FullRoundTripTests {

        @Test
        @DisplayName("Full flow: @Cacheable stores list → CacheService reads → CacheService stores map → @Cacheable reads map")
        void put_ShouldMaintainTypedData_WhenMixingCacheableAndCacheService() {
            // Step 1: @Cacheable stores list
            List<Template> templates = cacheableTestService.findAllActiveTemplates();
            assertEquals(3, templates.size());

            // Step 2: CacheService reads the list stored by @Cacheable
            Collection<Template> readTemplates = cacheService.get("template-service:allTemplates:all", List.class, Template.class);
            assertEquals(3, readTemplates.size());

            // Step 3: CacheService stores a map (no @class in values)
            Map<String, TemplateDto> manualMap = CacheableTestService.createSampleTemplateDtoMap();
            cacheService.put("template-service:templateByType:all", manualMap);

            // Step 4: @Cacheable reads the map stored by CacheService (cache hit, returns generic map)
            Map<String, TemplateDto> cacheableMap = cacheableTestService.getTemplateByType();
            assertNotNull(cacheableMap);
            assertEquals(3, cacheableMap.size());

            // Typed access via CacheService.getMap
            Map<String, TemplateDto> typedMap = cacheService.getMap("template-service:templateByType:all", String.class, TemplateDto.class);
            TemplateDto dto = typedMap.get("CUSTOM_MAIL_PDF");
            assertNotNull(dto);
            assertEquals(104L, dto.getId());
            assertEquals("custom_mail_pdf_template.xlsx", dto.getName());

            // Step 5: CacheService reads the same map with a different value type
            Map<String, Template> crossTypeMap = cacheService.getMap("template-service:templateByType:all", String.class, Template.class);
            assertNotNull(crossTypeMap);
            assertEquals(3, crossTypeMap.size());

            Template crossTemplate = crossTypeMap.get("CUSTOM_MAIL_PDF");
            assertNotNull(crossTemplate);
            assertEquals(104L, crossTemplate.getId());
        }

        @Test
        @DisplayName("@Cacheable second call should return cached data")
        void findAllActiveTemplates_ShouldReturnCachedData_WhenCalledMultipleTimes() {
            // First call — populates cache
            List<Template> first = cacheableTestService.findAllActiveTemplates();
            assertEquals(3, first.size());

            // Second call — should return from cache (same data)
            List<Template> second = cacheableTestService.findAllActiveTemplates();
            assertEquals(3, second.size());

            // Second call — should return from cache (verify cache key still exists)
            assertEquals(first.getFirst().getId(), second.getFirst().getId());
            assertEquals(first.getFirst().getName(), second.getFirst().getName());
        }
    }

    // ==================== CacheService.put() as TemplateDto → @Cacheable reads as TemplateSimpleDto ====================

    @Nested
    @DisplayName("CacheService stores TemplateDto, @Cacheable reads as TemplateSimpleDto")
    class CrossTypeCacheServicePutCacheableReadTests {

        @Test
        @DisplayName("@Cacheable should return TemplateSimpleDto map when CacheService stored TemplateDto map")
        void getTemplateByType2_ShouldReturnSimpleDto_WhenStoredAsTemplateDtoViaCacheService() {
            // Arrange — simulate: templateLoadingService.getTemplateByType2() without @Cacheable
            Map<String, TemplateDto> templateDtoMap = cacheableTestService.getTemplateByTypeWithoutCache();
            // Store via CacheService (same key that @Cacheable("template-service:templateByType2") uses)
            cacheService.put("template-service:templateByType2:all", templateDtoMap);

            // Act — @Cacheable reads the same key but returns Map<String, TemplateSimpleDto>
            Map<String, TemplateSimpleDto> result = cacheableTestService.getTemplateByType2();

            // Assertions — TemplateSimpleDto has subset of TemplateDto fields (id, name, type, active)
            assertNotNull(result);
            assertEquals(3, result.size());
            assertFalse(result.containsKey("@class"));

            TemplateSimpleDto dto = result.get("CUSTOM_MAIL_PDF");
            assertNotNull(dto);
            assertEquals(104L, dto.getId());
            assertEquals("custom_mail_pdf_template.xlsx", dto.getName());
            assertEquals("CUSTOM_MAIL_PDF", dto.getType());
            assertTrue(dto.getActive());
        }

        @Test
        @DisplayName("CacheService.getMap should return TemplateSimpleDto when CacheService stored TemplateDto map")
        void getMap_ShouldReturnSimpleDto_WhenStoredAsTemplateDtoViaCacheService() {
            // Arrange — store as TemplateDto
            Map<String, TemplateDto> templateDtoMap = CacheableTestService.createSampleTemplateDtoMap();
            cacheService.put("template-service:templateByType2:all", templateDtoMap);

            // Act — read as TemplateSimpleDto via CacheService.getMap
            Map<String, TemplateSimpleDto> result = cacheService.getMap("template-service:templateByType2:all", String.class, TemplateSimpleDto.class);

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());

            TemplateSimpleDto dto = result.get("CUSTOM_MAIL_PDF");
            assertNotNull(dto);
            assertEquals(104L, dto.getId());
            assertEquals("custom_mail_pdf_template.xlsx", dto.getName());
            assertEquals("CUSTOM_MAIL_PDF", dto.getType());
            assertTrue(dto.getActive());
        }

        @Test
        @DisplayName("Full flow: CacheService stores TemplateDto → @Cacheable reads as TemplateSimpleDto → CacheService reads as TemplateDto")
        void put_ShouldSupportCrossTypeReads_WhenStoredAsTemplateDtoReadAsMultipleTypes() {
            // Step 1: Non-cacheable method returns TemplateDto map
            Map<String, TemplateDto> originalMap = cacheableTestService.getTemplateByTypeWithoutCache();
            assertEquals(3, originalMap.size());

            // Step 2: Store via CacheService (same key as @Cacheable)
            cacheService.put("template-service:templateByType2:all", originalMap);

            // Step 3: @Cacheable reads as TemplateSimpleDto (cache hit — different type)
            Map<String, TemplateSimpleDto> simpleDtoMap = cacheableTestService.getTemplateByType2();
            assertNotNull(simpleDtoMap);
            assertEquals(3, simpleDtoMap.size());

            TemplateSimpleDto simpleDto = simpleDtoMap.get("COMPANY_RESEARCH_EXPORT");
            assertNotNull(simpleDto);
            assertEquals(130L, simpleDto.getId());
            assertEquals("company_research_export.xlsx", simpleDto.getName());

            // Step 4: CacheService reads the same key as original TemplateDto
            Map<String, TemplateDto> fullDtoMap = cacheService.getMap("template-service:templateByType2:all", String.class, TemplateDto.class);
            assertNotNull(fullDtoMap);
            assertEquals(3, fullDtoMap.size());

            TemplateDto fullDto = fullDtoMap.get("COMPANY_RESEARCH_EXPORT");
            assertNotNull(fullDto);
            assertEquals(130L, fullDto.getId());
            assertEquals("company_research_export.xlsx", fullDto.getName());
            assertEquals("7c638142-65fc-4dc2-abaf-a08a39fe2e30.xlsx", fullDto.getPath());
            assertEquals(1, fullDto.getVersion());
        }
    }

    // ==================== Primitive/Simple type collection and map tests ====================

    @Nested
    @DisplayName("Primitive/Simple type collections and maps via CacheService")
    class PrimitiveTypeTests {

        // ---- List ----

        @Test
        @DisplayName("Should store and retrieve List<Integer> via CacheService")
        void get_ShouldReturnIntegerList_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:list:integer";
            List<Integer> ids = List.of(1, 2, 3, 42, 100);

            // Act
            cacheService.put(key, ids);
            Collection<Integer> result = cacheService.get(key, List.class, Integer.class);

            // Assertions
            assertNotNull(result);
            List<Integer> resultList = new ArrayList<>(result);
            assertEquals(5, resultList.size());
            assertEquals(1, resultList.get(0));
            assertEquals(42, resultList.get(3));
            assertEquals(100, resultList.get(4));
        }

        @Test
        @DisplayName("Should store and retrieve List<Long> via CacheService")
        void get_ShouldReturnLongList_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:list:long";
            List<Long> ids = List.of(100L, 200L, 300L);

            // Act
            cacheService.put(key, ids);
            Collection<Long> result = cacheService.get(key, List.class, Long.class);

            // Assertions
            assertNotNull(result);
            List<Long> resultList = new ArrayList<>(result);
            assertEquals(3, resultList.size());
            assertEquals(100L, resultList.get(0));
            assertEquals(200L, resultList.get(1));
            assertEquals(300L, resultList.get(2));
        }

        @Test
        @DisplayName("Should store and retrieve List<String> via CacheService")
        void get_ShouldReturnStringList_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:list:string";
            List<String> names = List.of("CUSTOM_MAIL_PDF", "COMPANY_RESEARCH_EXPORT", "EXCEL_ACTIVITY_STATUS");

            // Act
            cacheService.put(key, names);
            Collection<String> result = cacheService.get(key, List.class, String.class);

            // Assertions
            assertNotNull(result);
            List<String> resultList = new ArrayList<>(result);
            assertEquals(3, resultList.size());
            assertEquals("CUSTOM_MAIL_PDF", resultList.get(0));
            assertEquals("COMPANY_RESEARCH_EXPORT", resultList.get(1));
            assertEquals("EXCEL_ACTIVITY_STATUS", resultList.get(2));
        }

        @Test
        @DisplayName("Should store and retrieve List<Double> via CacheService")
        void get_ShouldReturnDoubleList_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:list:double";
            List<Double> values = List.of(1.5, 2.7, 3.14);

            // Act
            cacheService.put(key, values);
            Collection<Double> result = cacheService.get(key, List.class, Double.class);

            // Assertions
            assertNotNull(result);
            List<Double> resultList = new ArrayList<>(result);
            assertEquals(3, resultList.size());
            assertEquals(1.5, resultList.get(0));
            assertEquals(2.7, resultList.get(1));
            assertEquals(3.14, resultList.get(2));
        }

        @Test
        @DisplayName("Should store and retrieve List<Boolean> via CacheService")
        void get_ShouldReturnBooleanList_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:list:boolean";
            List<Boolean> flags = List.of(true, false, true);

            // Act
            cacheService.put(key, flags);
            Collection<Boolean> result = cacheService.get(key, List.class, Boolean.class);

            // Assertions
            assertNotNull(result);
            List<Boolean> resultList = new ArrayList<>(result);
            assertEquals(3, resultList.size());
            assertTrue(resultList.get(0));
            assertFalse(resultList.get(1));
            assertTrue(resultList.get(2));
        }

        // ---- Set ----

        @Test
        @DisplayName("Should store List<Integer> and read as Set<Integer> via CacheService")
        void get_ShouldReturnIntegerSet_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:set:integer";
            List<Integer> ids = List.of(10, 20, 30);

            // Act
            cacheService.put(key, ids);
            Collection<Integer> result = cacheService.get(key, Set.class, Integer.class);

            // Assertions
            assertNotNull(result);
            assertInstanceOf(Set.class, result);
            assertEquals(3, result.size());
            assertTrue(result.contains(10));
            assertTrue(result.contains(20));
            assertTrue(result.contains(30));
        }

        @Test
        @DisplayName("Should store List<String> and read as Set<String> via CacheService")
        void get_ShouldReturnStringSet_WhenStoredAsList() {
            // Arrange
            String key = "test:cross:primitive:set:string";
            List<String> names = List.of("A", "B", "C");

            // Act
            cacheService.put(key, names);
            Collection<String> result = cacheService.get(key, Set.class, String.class);

            // Assertions
            assertNotNull(result);
            assertInstanceOf(Set.class, result);
            assertEquals(3, result.size());
            assertTrue(result.contains("A"));
            assertTrue(result.contains("B"));
            assertTrue(result.contains("C"));
        }

        // ---- Map with primitive values ----

        @Test
        @DisplayName("Should store and retrieve Map<String, Integer> via CacheService.getMap")
        void getMap_ShouldReturnIntegerValues_WhenStoredAsMap() {
            // Arrange
            String key = "test:cross:primitive:map:integer";
            Map<String, Integer> map = new LinkedHashMap<>();
            map.put("TEMPLATE_A", 104);
            map.put("TEMPLATE_B", 130);
            map.put("TEMPLATE_C", 47);

            // Act
            cacheService.put(key, map);
            Map<String, Integer> result = cacheService.getMap(key, String.class, Integer.class);

            // Assertions
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(104, result.get("TEMPLATE_A"));
            assertEquals(130, result.get("TEMPLATE_B"));
            assertEquals(47, result.get("TEMPLATE_C"));
        }

        @Test
        @DisplayName("Should store and retrieve Map<String, String> via CacheService.getMap")
        void getMap_ShouldReturnStringValues_WhenStoredAsMap() {
            // Arrange
            String key = "test:cross:primitive:map:string";
            Map<String, String> map = new LinkedHashMap<>();
            map.put("CUSTOM_MAIL_PDF", "custom_mail_pdf_template.xlsx");
            map.put("COMPANY_RESEARCH_EXPORT", "company_research_export.xlsx");

            // Act
            cacheService.put(key, map);
            Map<String, String> result = cacheService.getMap(key, String.class, String.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("custom_mail_pdf_template.xlsx", result.get("CUSTOM_MAIL_PDF"));
            assertEquals("company_research_export.xlsx", result.get("COMPANY_RESEARCH_EXPORT"));
        }

        @Test
        @DisplayName("Should store and retrieve Map<String, Long> via CacheService.getMap")
        void getMap_ShouldReturnLongValues_WhenStoredAsMap() {
            // Arrange
            String key = "test:cross:primitive:map:long";
            Map<String, Long> map = new LinkedHashMap<>();
            map.put("TEMPLATE_A", 104L);
            map.put("TEMPLATE_B", 130L);

            // Act
            cacheService.put(key, map);
            Map<String, Long> result = cacheService.getMap(key, String.class, Long.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(104L, result.get("TEMPLATE_A"));
            assertEquals(130L, result.get("TEMPLATE_B"));
        }

        @Test
        @DisplayName("Should store and retrieve Map<String, Boolean> via CacheService.getMap")
        void getMap_ShouldReturnBooleanValues_WhenStoredAsMap() {
            // Arrange
            String key = "test:cross:primitive:map:boolean";
            Map<String, Boolean> map = new LinkedHashMap<>();
            map.put("ACTIVE", true);
            map.put("DELETED", false);

            // Act
            cacheService.put(key, map);
            Map<String, Boolean> result = cacheService.getMap(key, String.class, Boolean.class);

            // Assertions
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.get("ACTIVE"));
            assertFalse(result.get("DELETED"));
        }

        // ---- Mixed: primitives with complex objects ----

        @Test
        @DisplayName("Should store List<Integer> and read as List<Integer> via CacheService, then store List<Template> and read correctly")
        void get_ShouldHandleBothPrimitiveAndComplexLists_WhenStoredSequentially() {
            // Arrange — primitive list
            String primitiveKey = "test:cross:primitive:mixed:integers";
            List<Integer> ids = List.of(104, 130, 47);
            cacheService.put(primitiveKey, ids);

            // Arrange — complex list
            String complexKey = "test:cross:primitive:mixed:templates";
            List<Template> templates = CacheableTestService.createSampleTemplates();
            cacheService.put(complexKey, templates);

            // Act & Assert — primitive list
            Collection<Integer> intResult = cacheService.get(primitiveKey, List.class, Integer.class);
            assertNotNull(intResult);
            assertEquals(3, intResult.size());
            List<Integer> intList = new ArrayList<>(intResult);
            assertEquals(104, intList.getFirst());

            // Act & Assert — complex list
            Collection<Template> templateResult = cacheService.get(complexKey, List.class, Template.class);
            assertNotNull(templateResult);
            assertEquals(3, templateResult.size());
            List<Template> templateList = new ArrayList<>(templateResult);
            assertEquals(104L, templateList.getFirst().getId());
            assertEquals("custom_mail_pdf_template.xlsx", templateList.getFirst().getName());
        }

        @Test
        @DisplayName("Should store empty List<Integer> and retrieve empty collection")
        void get_ShouldReturnEmptyList_WhenStoredAsEmptyIntegerList() {
            // Arrange
            String key = "test:cross:primitive:list:empty";
            List<Integer> emptyList = List.of();

            // Act
            cacheService.put(key, emptyList);
            Collection<Integer> result = cacheService.get(key, List.class, Integer.class);

            // Assertions
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
