package com.mb.livedataservice.client.jsonplaceholder;

import com.mb.livedataservice.client.jsonplaceholder.request.PostRequest;
import com.mb.livedataservice.client.jsonplaceholder.response.PostResponse;
import com.mb.livedataservice.config.JSONPlaceholderClientProperties;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.ErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class JSONPlaceholderRestClient {

    private final RestClient restClient;

    public JSONPlaceholderRestClient(RestClient.Builder builder, JSONPlaceholderClientProperties JSONPlaceholderClientProperties) {
        JdkClientHttpRequestFactory jdkClientHttpRequestFactory = new JdkClientHttpRequestFactory();
        jdkClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = builder
                .baseUrl(JSONPlaceholderClientProperties.getUrl())
                .requestFactory(jdkClientHttpRequestFactory)
                .requestInterceptor(new BasicAuthenticationInterceptor(JSONPlaceholderClientProperties.getClientId(), JSONPlaceholderClientProperties.getClientSecret()))
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
    }

    public String findAllTodos() {
        return restClient.get()
                .uri("/todos")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public List<PostResponse> findAllPosts() {
        return restClient.get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public PostResponse createPost(PostRequest postRequest) {
        return restClient.post()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .body(postRequest)
                .retrieve()
                .body(PostResponse.class);
    }

    public PostResponse getPostById(int id) {
        return restClient.get()
                .uri("/posts/%d".formatted(id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(PostResponse.class);
    }
}
