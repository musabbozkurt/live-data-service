package com.mb.livedataservice.integration_tests.config.redis.serializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test service that simulates a repository/service with @Cacheable annotations.
 * Used by {@link CacheServiceAndCacheableIntegrationTest} to verify that data stored
 * via CacheService can be read by @Cacheable and vice versa.
 * <p>
 * Redis cache key convention: {@code service-name:cache-value:cache-key}
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code template-service:allTemplates:all}</li>
 *   <li>{@code template-service:templateByType:all}</li>
 *   <li>{@code template-service:singleTemplate:CUSTOM_MAIL_PDF}</li>
 * </ul>
 */
@Service
public class CacheableTestService {

    private static final String ALL_TEMPLATES_CACHE = "template-service:allTemplates";
    private static final String TEMPLATE_BY_TYPE_CACHE = "template-service:templateByType";
    private static final String SINGLE_TEMPLATE_CACHE = "template-service:singleTemplate";
    private static final String TEMPLATE_BY_TYPE2_CACHE = "template-service:templateByType2";

    // ==================== Model Classes ====================

    public static List<Template> createSampleTemplates() {
        return List.of(
                Template.builder()
                        .id(104L)
                        .name("custom_mail_pdf_template.xlsx")
                        .type("CUSTOM_MAIL_PDF")
                        .path("b025101b-2128-4975-9893-f2f2548ddda7.xlsx")
                        .active(true)
                        .version(1)
                        .audit(AuditInfo.builder()
                                .createdBy("test-user")
                                .createdDate(LocalDateTime.of(2025, 11, 18, 9, 53, 40))
                                .lastModifiedBy("test-user")
                                .lastModifiedDate(LocalDateTime.of(2025, 11, 18, 9, 53, 40))
                                .build()
                        )
                        .build(),
                Template.builder()
                        .id(130L)
                        .name("company_research_export.xlsx")
                        .type("COMPANY_RESEARCH_EXPORT")
                        .path("7c638142-65fc-4dc2-abaf-a08a39fe2e30.xlsx")
                        .active(true)
                        .version(1)
                        .audit(AuditInfo.builder()
                                .createdBy("test-user")
                                .createdDate(LocalDateTime.of(2025, 12, 23, 10, 38, 23))
                                .lastModifiedBy("test-user")
                                .lastModifiedDate(LocalDateTime.of(2025, 12, 23, 10, 38, 23))
                                .build()
                        )
                        .build(),
                Template.builder()
                        .id(47L)
                        .name("aktiflik_durumu.xlsx")
                        .type("EXCEL_ACTIVITY_STATUS")
                        .path("45043ef6-2c42-49ef-a4f7-fecf92567d30.xlsx")
                        .active(true)
                        .version(1)
                        .audit(AuditInfo.builder()
                                .createdBy("test-user")
                                .createdDate(LocalDateTime.of(2025, 7, 7, 17, 1, 36))
                                .lastModifiedBy("test-user")
                                .lastModifiedDate(LocalDateTime.of(2025, 7, 7, 17, 1, 36))
                                .build()
                        )
                        .build()
        );
    }

    public static Map<String, TemplateDto> createSampleTemplateDtoMap() {
        Map<String, TemplateDto> map = new LinkedHashMap<>();
        map.put("CUSTOM_MAIL_PDF",
                TemplateDto.builder()
                        .id(104L)
                        .name("custom_mail_pdf_template.xlsx")
                        .type("CUSTOM_MAIL_PDF")
                        .path("b025101b-2128-4975-9893-f2f2548ddda7.xlsx")
                        .active(true)
                        .version(1)
                        .build()
        );
        map.put("COMPANY_RESEARCH_EXPORT",
                TemplateDto.builder()
                        .id(130L)
                        .name("company_research_export.xlsx")
                        .type("COMPANY_RESEARCH_EXPORT")
                        .path("7c638142-65fc-4dc2-abaf-a08a39fe2e30.xlsx")
                        .active(true)
                        .version(1)
                        .build()
        );
        map.put("EXCEL_ACTIVITY_STATUS",
                TemplateDto.builder()
                        .id(47L)
                        .name("aktiflik_durumu.xlsx")
                        .type("EXCEL_ACTIVITY_STATUS")
                        .path("45043ef6-2c42-49ef-a4f7-fecf92567d30.xlsx")
                        .active(true)
                        .version(1)
                        .build()
        );
        return map;
    }

    public static Map<String, TemplateSimpleDto> createSampleTemplateSimpleDtoMap() {
        Map<String, TemplateSimpleDto> map = new LinkedHashMap<>();
        map.put("CUSTOM_MAIL_PDF",
                TemplateSimpleDto.builder()
                        .id(104L)
                        .name("custom_mail_pdf_template.xlsx")
                        .type("CUSTOM_MAIL_PDF")
                        .active(true)
                        .build()
        );
        map.put("COMPANY_RESEARCH_EXPORT",
                TemplateSimpleDto.builder()
                        .id(130L)
                        .name("company_research_export.xlsx")
                        .type("COMPANY_RESEARCH_EXPORT")
                        .active(true)
                        .build()
        );
        map.put("EXCEL_ACTIVITY_STATUS",
                TemplateSimpleDto.builder()
                        .id(47L).name("aktiflik_durumu.xlsx")
                        .type("EXCEL_ACTIVITY_STATUS")
                        .active(true)
                        .build()
        );
        return map;
    }

    /**
     * Simulates: @Cacheable returning List of Template (like findAllByActiveIsTrue)
     */
    @Cacheable(value = ALL_TEMPLATES_CACHE, key = "'all'")
    public List<Template> findAllActiveTemplates() {
        return createSampleTemplates();
    }

    // ==================== @Cacheable methods ====================

    /**
     * Simulates: @Cacheable returning Map of TemplateDto (like getTemplateByType)
     */
    @Cacheable(value = TEMPLATE_BY_TYPE_CACHE, key = "'all'")
    public Map<String, TemplateDto> getTemplateByType() {
        return createSampleTemplateDtoMap();
    }

    /**
     * Simulates: @Cacheable returning a single object
     */
    @Cacheable(value = SINGLE_TEMPLATE_CACHE, key = "#type")
    public Template getTemplateByTypeSingle(String type) {
        return createSampleTemplates().stream()
                .filter(t -> t.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * Simulates a method WITHOUT @Cacheable — the caller stores the result manually via CacheService.
     * Returns Map of TemplateDto (same data as getTemplateByType).
     */
    public Map<String, TemplateDto> getTemplateByTypeWithoutCache() {
        return createSampleTemplateDtoMap();
    }

    /**
     * Simulates: @Cacheable returning Map of TemplateSimpleDto (a different DTO type).
     * Uses the same cache name as data stored via CacheService by getTemplateByTypeWithoutCache.
     * This tests cross-type compatibility: data stored as TemplateDto, read as TemplateSimpleDto.
     */
    @Cacheable(value = TEMPLATE_BY_TYPE2_CACHE, key = "'all'")
    public Map<String, TemplateSimpleDto> getTemplateByType2() {
        return createSampleTemplateSimpleDtoMap();
    }

    @CacheEvict(value = ALL_TEMPLATES_CACHE, allEntries = true)
    public void evictAllTemplates() {
        // no-op, just evict
    }

    // ==================== @CacheEvict methods ====================

    @CacheEvict(value = TEMPLATE_BY_TYPE_CACHE, allEntries = true)
    public void evictTemplateByType() {
        // no-op, just evict
    }

    @CacheEvict(value = SINGLE_TEMPLATE_CACHE, allEntries = true)
    public void evictSingleTemplate() {
        // no-op, just evict
    }

    @CacheEvict(value = TEMPLATE_BY_TYPE2_CACHE, allEntries = true)
    public void evictTemplateByType2() {
        // no-op, just evict
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateDto {
        private Long id;
        private String name;
        private String type;
        private String path;
        private Boolean active;
        private Integer version;
    }

    // ==================== Sample Data Factories ====================

    /**
     * A different DTO with a subset of fields — simulates reading cached data as a different type
     * (e.g. reading TemplateDto data as ASD2).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateSimpleDto {
        private Long id;
        private String name;
        private String type;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Template {
        private Long id;
        private String name;
        private String type;
        private String path;
        private Boolean active;
        private Integer version;
        private AuditInfo audit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditInfo {
        private String createdBy;
        private LocalDateTime createdDate;
        private String lastModifiedBy;
        private LocalDateTime lastModifiedDate;
    }
}
