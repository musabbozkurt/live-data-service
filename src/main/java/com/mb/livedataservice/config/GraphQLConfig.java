package com.mb.livedataservice.config;

import graphql.scalars.ExtendedScalars;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.graphql.autoconfigure.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Slf4j
@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> {
            wiringBuilder.scalar(ExtendedScalars.Date);
            wiringBuilder.scalar(ExtendedScalars.Url);
        };
    }

    @Bean
    public GraphQlSourceBuilderCustomizer inspectionCustomizer() {
        return source -> source.inspectSchemaMappings(report -> log.info(report.toString()));
    }
}
