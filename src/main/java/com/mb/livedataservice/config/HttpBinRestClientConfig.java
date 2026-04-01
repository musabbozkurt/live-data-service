package com.mb.livedataservice.config;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpBinRestClientConfig {

    @Value("${httpbin.base-url:https://httpbin.org}")
    private String baseUrl;

    /**
     * @param builder RestClient.Builder to use for building the RestClient. Spring will automatically inject it.
     * @return RestClient configured with base URL and error handling for HTTP 404 and unexpected errors.
     */
    @Bean
    public RestClient httpBinRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new BaseException(LiveDataErrorCode.NOT_FOUND);
                    }
                    throw new BaseException(LiveDataErrorCode.UNEXPECTED_ERROR);
                })
                .build();
    }
}
