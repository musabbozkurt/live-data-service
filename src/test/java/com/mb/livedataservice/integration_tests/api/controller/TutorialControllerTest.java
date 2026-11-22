package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.client.jsonplaceholder.DeclarativeJSONPlaceholderRestClient;
import com.mb.livedataservice.client.jsonplaceholder.JSONPlaceholderRestClient;
import com.mb.livedataservice.client.jsonplaceholder.request.PostRequest;
import com.mb.livedataservice.client.jsonplaceholder.response.PostResponse;
import com.mb.livedataservice.exception.ErrorResponse;
import com.mb.livedataservice.helper.RestResponsePage;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureRestTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestcontainersConfiguration.class)
class TutorialControllerTest extends BaseUnitTest {

    private static Long tutorialId = 0L;

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private JSONPlaceholderRestClient placeholderRestClient;

    @Autowired
    private DeclarativeJSONPlaceholderRestClient declarativeJSONPlaceholderRestClient;

    @BeforeAll
    static void setup(@Autowired RestTestClient client) {
        // Prepare some test data
        for (int i = 0; i <= 100; i++) {
            ApiTutorialRequest apiTutorialRequest = new ApiTutorialRequest("Spring Boot @WebMvcTest%d".formatted(i), "Description%d".formatted(i), true);
            client.post().uri("/api/tutorials").body(apiTutorialRequest).exchange().expectStatus().isCreated();
        }

        EntityExchangeResult<ApiTutorialResponse[]> response = client.get().uri("/api/tutorials").exchange().expectStatus().isOk().expectBody(ApiTutorialResponse[].class).returnResult();
        ApiTutorialResponse[] tutorials = Objects.requireNonNull(response.getResponseBody());

        assertThat(tutorials).hasSizeGreaterThan(100);

        tutorialId = Arrays.stream(tutorials).toList().getFirst().getId();
    }

    @Test
    void shouldCreateNewTutorialWhenTutorialIsValid() {
        ApiTutorialRequest apiTutorialRequest = getApiTutorialRequest();

        EntityExchangeResult<ApiTutorialResponse> response = restTestClient.post().uri("/api/tutorials").body(apiTutorialRequest).exchange().expectStatus().isCreated().expectBody(ApiTutorialResponse.class).returnResult();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getResponseBody()).isNotNull();
        assertThat(response.getResponseBody().getId()).isGreaterThan(0);
        assertThat(response.getResponseBody().getTitle()).isEqualTo("Spring Boot @WebMvcTest");
        assertThat(response.getResponseBody().getDescription()).isEqualTo("Description");
    }

    @Test
    void shouldGetTutorialWhenValidTutorialId() {
        EntityExchangeResult<ApiTutorialResponse> response = restTestClient.get().uri("/api/tutorials/%d".formatted(tutorialId)).exchange().expectStatus().isOk().expectBody(ApiTutorialResponse.class).returnResult();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getResponseBody()).isNotNull();
        assertThat(response.getResponseBody().getTitle()).isEqualTo("Spring Boot @WebMvcTest0");
        assertThat(response.getResponseBody().getDescription()).isEqualTo("Description0");
    }

    @Test
    void shouldGetAllTutorials() {
        EntityExchangeResult<ApiTutorialResponse[]> response = restTestClient.get().uri("/api/tutorials").exchange().expectStatus().isOk().expectBody(ApiTutorialResponse[].class).returnResult();

        assertThat(response.getResponseBody()).isNotNull().hasSizeGreaterThan(100);
    }

    @Test
    void shouldThrowNotFoundWhenInvalidTutorialId() {
        EntityExchangeResult<ErrorResponse> response = restTestClient.get().uri("/api/tutorials/999").exchange().expectStatus().isNotFound().expectBody(ErrorResponse.class).returnResult();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateTutorialWhenTutorialIsValid() {
        ApiTutorialUpdateRequest apiTutorialUpdateRequest = getApiTutorialUpdateRequest();

        EntityExchangeResult<ApiTutorialResponse> updatedResponse = restTestClient.put().uri("/api/tutorials/%d".formatted(tutorialId)).body(apiTutorialUpdateRequest).exchange().expectStatus().isOk().expectBody(ApiTutorialResponse.class).returnResult();

        assertThat(updatedResponse).isNotNull();
        assertThat(updatedResponse.getResponseBody()).isNotNull();

        assertThat(updatedResponse.getResponseBody().getId()).isEqualTo(tutorialId);
        assertThat(updatedResponse.getResponseBody().getTitle()).isEqualTo("Updated");
        assertThat(updatedResponse.getResponseBody().getDescription()).isEqualTo("Updated");
    }

    @Test
    @Order(Integer.MAX_VALUE - 1)
    void shouldDeleteWithValidId() {
        ExchangeResult response = restTestClient.delete().uri("/api/tutorials/88").exchange().expectStatus().isNoContent().returnResult();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void shouldDeleteAllTutorials() {
        ExchangeResult response = restTestClient.delete().uri("/api/tutorials").exchange().expectStatus().isNoContent().returnResult();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldGetAllTutorialsByPublishedTrue() {
        EntityExchangeResult<ApiTutorialResponse[]> response = restTestClient.get().uri("/api/tutorials/published").exchange().expectStatus().isOk().expectBody(ApiTutorialResponse[].class).returnResult();

        assertThat(response.getResponseBody()).isNotNull().hasSizeGreaterThan(100);
    }

    @Test
    void shouldGetAllTutorialsByFilter() {
        ParameterizedTypeReference<RestResponsePage<ApiTutorialResponse>> responseType = new ParameterizedTypeReference<>() {
        };

        UriComponents uriComponents = UriComponentsBuilder.fromPath("/api/tutorials/filter")
                .queryParam("pageSize", "2")
                .queryParam("page", "0")
                .queryParam("description", "Description1")
                .queryParam("published", true)
                .build();

        EntityExchangeResult<RestResponsePage<ApiTutorialResponse>> tutorials = restTestClient.get().uri(uriComponents.toString()).exchange().expectStatus().isOk().expectBody(responseType).returnResult();
        RestResponsePage<ApiTutorialResponse> tutorialsBody = tutorials.getResponseBody();

        assertThat(tutorials.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(tutorialsBody).isNotNull();
        assertThat(tutorialsBody.getNumberOfElements()).isPositive();
    }

    @Test
    void shouldFindAllTodos() {
        String todos = placeholderRestClient.findAllTodos();
        assertNotNull(todos);
    }

    @Test
    void shouldFindAllPosts() {
        List<PostResponse> posts = placeholderRestClient.findAllPosts();
        assertNotNull(posts);
        assertThat(posts).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldCreatePost() {
        PostRequest newPost = new PostRequest(5, null, "The Lord of the Rings", null);

        PostResponse post = placeholderRestClient.createPost(newPost);
        assertNotNull(post);
        assertThat(post.userId()).isEqualTo(newPost.userId());
        assertThat(post.title()).isEqualTo(newPost.title());
    }

    @Test
    void shouldGetPostById() {
        PostResponse post = placeholderRestClient.getPostById(5);
        assertNotNull(post);
    }

    @Test
    void shouldGetAllPosts() {
        List<PostResponse> posts = declarativeJSONPlaceholderRestClient.getAllPosts();
        assertThat(posts).hasSize(100);
    }

    @Test
    void shouldGetSinglePost() {
        PostResponse post = declarativeJSONPlaceholderRestClient.getPost(5);
        assertThat(post.id()).isEqualTo(5);
    }

    @Test
    void shouldDeleteSinglePost() {
        ResponseEntity<Void> response = declarativeJSONPlaceholderRestClient.deletePost(5);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldCreateNewPost() {
        PostRequest newPost = new PostRequest(1, null, "new title", "new body");
        PostResponse post = declarativeJSONPlaceholderRestClient.createPost(newPost);
        assertThat(post.title()).isEqualTo("new title");
    }
}
