package com.mb.livedataservice.config;

import com.mb.livedataservice.client.jsonplaceholder.DeclarativeJSONPlaceholderRestClient;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.ErrorCode;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class DeclarativeHttpInterfaces {

    @Bean
    public DeclarativeJSONPlaceholderRestClient declarativeJSONPlaceholderRestClient(RestClient.Builder builder, JSONPlaceholderClientProperties placeholderClientProperties) {
        JdkClientHttpRequestFactory jdkClientHttpRequestFactory = new JdkClientHttpRequestFactory();
        jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(15));

        RestClient restClient = builder
                .baseUrl(placeholderClientProperties.getUrl())
                .requestFactory(jdkClientHttpRequestFactory)
                .requestInterceptor(new BasicAuthenticationInterceptor(placeholderClientProperties.getClientId(), placeholderClientProperties.getClientSecret()))
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new BaseException(new ErrorCode() {

                        @Override
                        @SneakyThrows
                        public HttpStatus getHttpStatus() {
                            return HttpStatus.resolve(response.getStatusCode().value());
                        }

                        @Override
                        @SneakyThrows
                        public String getCode() {
                            return response.getStatusText();
                        }
                    });
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(DeclarativeJSONPlaceholderRestClient.class);
    }
}
