package com.mb.livedataservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.OperationService;
import org.springdoc.webmvc.api.MultipleOpenApiWebMvcResource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "swagger.documentation")
@EnableConfigurationProperties({SwaggerConfig.class, SwaggerConfig.SwaggerServices.class})
public class SwaggerConfig {

    @Getter
    @Setter
    public List<SwaggerServices> services;

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI();
    }

    @Bean()
    MultipleOpenApiWebMvcResource multipleOpenApiResource(List<GroupedOpenApi> groupedOpenApis,
                                                          ObjectFactory<OpenAPIService> defaultOpenAPIBuilder,
                                                          AbstractRequestService requestBuilder,
                                                          GenericResponseService responseBuilder,
                                                          OperationService operationParser,
                                                          SpringDocConfigProperties springDocConfigProperties,
                                                          SpringDocProviders springDocProviders,
                                                          SpringDocCustomizers springDocCustomizers) {

        services.forEach(swaggerService -> groupedOpenApis.add(GroupedOpenApi.builder()
                .group(swaggerService.getName())
                .pathsToMatch("/**")
                .pathsToExclude("/actuator/**")
                .build()));

        return new MultipleOpenApiWebMvcResource(groupedOpenApis, defaultOpenAPIBuilder, requestBuilder, responseBuilder, operationParser, springDocConfigProperties, springDocProviders, springDocCustomizers);
    }

    @Data
    @ToString
    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "swagger.documentation.services")
    public static class SwaggerServices {
        private String name;
        private String url;
        private String version;
    }

}