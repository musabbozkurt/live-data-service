package com.mb.livedataservice.config;

import com.mb.livedataservice.client.jsonplaceholder.TodoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(types = {TodoService.class})
public class SpringBoot4RestClientConfig {

    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer() {
        return groups -> groups.forEachClient((_, builder) -> builder
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .build());
    }

    /**
     * Example configuration for multiple HTTP service groups.
     * <p>
     * To configure multiple groups, use the {@code @ImportHttpServices} annotation
     * multiple times with different group names:
     * </p>
     * <pre>
     * &#64;ImportHttpServices(group = "github", types = {GithubService.class})
     * &#64;ImportHttpServices(group = "jsonplaceholder", types = {TodoService.class, PostService.class})
     * </pre>
     *
     * @see ImportHttpServices
     */
    @Bean
    RestClientHttpServiceGroupConfigurer multipleGroupConfigurer() {
        return groups -> {
            groups.filterByName("github")
                    .forEachClient((_, b) -> b
                            .baseUrl("https://api.github.com")
                            .defaultHeader("Accept", "application/vnd.github.v3+json")
                            .build()
                    );

            groups.filterByName("jsonplaceholder")
                    .forEachClient((_, b) -> b
                            .baseUrl("https://jsonplaceholder.typicode.com/")
                            .build()
                    );
        };
    }
}
