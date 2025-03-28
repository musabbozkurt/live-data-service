package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiCarResponse;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.client.jsonplaceholder.DeclarativeJSONPlaceholderRestClient;
import com.mb.livedataservice.client.jsonplaceholder.JSONPlaceholderRestClient;
import com.mb.livedataservice.client.jsonplaceholder.request.PostRequest;
import com.mb.livedataservice.client.jsonplaceholder.response.PostResponse;
import com.mb.livedataservice.data.model.Tutorial;
import com.mb.livedataservice.helper.RestResponsePage;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestcontainersConfiguration.class)
class TutorialControllerTest extends BaseUnitTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JSONPlaceholderRestClient placeholderRestClient;

    @Autowired
    private DeclarativeJSONPlaceholderRestClient declarativeJSONPlaceholderRestClient;

    @BeforeAll
    static void setup(@Autowired TestRestTemplate restTemplate) {
        //Prepare some test data
        for (int i = 0; i <= 100; i++) {
            ApiTutorialRequest apiTutorialRequest = new ApiTutorialRequest("Spring Boot @WebMvcTest%d".formatted(i), "Description%d".formatted(i), true);
            restTemplate.exchange("/api/tutorials", HttpMethod.POST, new HttpEntity<>(apiTutorialRequest), ApiTutorialResponse.class);
        }
    }

    @Test
    @Rollback
    void shouldCreateNewTutorialWhenTutorialIsValid() {
        ApiTutorialRequest apiTutorialRequest = getApiTutorialRequest();

        ResponseEntity<ApiTutorialResponse> response = restTemplate.exchange("/api/tutorials", HttpMethod.POST, new HttpEntity<>(apiTutorialRequest), ApiTutorialResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(102);
        assertThat(response.getBody().getTitle()).isEqualTo("Spring Boot @WebMvcTest");
        assertThat(response.getBody().getDescription()).isEqualTo("Description");
    }

    @Test
    void shouldNotCreateNewTutorialWhenValidationFails() {
        ApiTutorialRequest apiTutorialRequest = new ApiTutorialRequest("Spring Boot @WebMvcTest", null, true);

        ResponseEntity<ApiTutorialResponse> response = restTemplate.exchange("/api/tutorials", HttpMethod.POST, new HttpEntity<>(apiTutorialRequest), ApiTutorialResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetTutorialWhenValidTutorialId() {
        ResponseEntity<Tutorial> response = restTemplate.exchange("/api/tutorials/1", HttpMethod.GET, null, Tutorial.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Spring Boot @WebMvcTest0");
        assertThat(response.getBody().getDescription()).isEqualTo("Description0");
    }

    @Test
    void shouldGetAllTutorials() {
        Tutorial[] tutorials = restTemplate.getForObject("/api/tutorials", Tutorial[].class);

        assertThat(tutorials).hasSizeGreaterThan(100);
    }

    @Test
    void shouldThrowNotFoundWhenInvalidTutorialId() {
        ResponseEntity<Tutorial> response = restTemplate.exchange("/api/tutorials/999", HttpMethod.GET, null, Tutorial.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Rollback
    void shouldUpdateTutorialWhenTutorialIsValid() {
        ApiTutorialUpdateRequest apiTutorialUpdateRequest = getApiTutorialUpdateRequest();

        ResponseEntity<ApiTutorialResponse> updatedResponse = restTemplate.exchange("/api/tutorials/99", HttpMethod.PUT, new HttpEntity<>(apiTutorialUpdateRequest), ApiTutorialResponse.class);

        assertThat(updatedResponse).isNotNull();
        assertThat(updatedResponse.getBody()).isNotNull();

        assertThat(updatedResponse.getBody().getId()).isEqualTo(99);
        assertThat(updatedResponse.getBody().getTitle()).isEqualTo("Updated");
        assertThat(updatedResponse.getBody().getDescription()).isEqualTo("Updated");
    }

    @Test
    @Rollback
    void shouldDeleteWithValidId() {
        ResponseEntity<Void> response = restTemplate.exchange("/api/tutorials/88", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Rollback
    @Disabled("This test will delete all tutorials, so it should be disabled.")
    void shouldDeleteAllTutorials() {
        ResponseEntity<HttpStatus> response = restTemplate.exchange("/api/tutorials", HttpMethod.DELETE, null, HttpStatus.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldGetAllTutorialsByPublishedTrue() {
        Tutorial[] tutorials = restTemplate.getForObject("/api/tutorials/published", Tutorial[].class);

        assertThat(tutorials).hasSizeGreaterThan(100);
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

        ResponseEntity<RestResponsePage<ApiTutorialResponse>> tutorials = restTemplate.exchange(uriComponents.toString(), HttpMethod.GET, null, responseType);
        RestResponsePage<ApiTutorialResponse> tutorialsBody = tutorials.getBody();

        assertThat(tutorials.getStatusCode()).isEqualTo(HttpStatus.OK);
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

    @Test
    void shouldDoFuzzySearchByFilter() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("model", "model");
        queryParams.put("yearOfManufacture", "2000");
        queryParams.put("brand", "brand");

        ApiCarResponse[] tutorials = restTemplate.getForObject("/cars/fuzzy-search?model={model}&yearOfManufacture={yearOfManufacture}&brand={brand}", ApiCarResponse[].class, queryParams);

        assertThat(tutorials.length).isNotNegative();
    }
}
