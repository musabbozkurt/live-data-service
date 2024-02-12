package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.client.jsonplaceholder.DeclarativeJSONPlaceholderRestClient;
import com.mb.livedataservice.client.jsonplaceholder.JSONPlaceholderRestClient;
import com.mb.livedataservice.client.jsonplaceholder.request.PostRequest;
import com.mb.livedataservice.client.jsonplaceholder.response.PostResponse;
import com.mb.livedataservice.data.model.Tutorial;
import com.mb.livedataservice.helper.RestResponsePage;
import com.mb.livedataservice.integration_tests.containers.DefaultElasticsearchContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TutorialControllerTest extends BaseUnitTest {

    @Container
    @ServiceConnection
    public static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:7.2.4")).withExposedPorts(6379);

    @Container
    @ServiceConnection
    public static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1");

    @Container
    private static final ElasticsearchContainer elasticsearchContainer = new DefaultElasticsearchContainer();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JSONPlaceholderRestClient JSONPlaceholderRestClient;

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
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
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

        assertThat(tutorials.length).isGreaterThan(100);
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

        assertThat(tutorials.length).isGreaterThan(100);
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
        assertThat(tutorialsBody.getNumberOfElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldFindAllTodos() {
        String todos = JSONPlaceholderRestClient.findAllTodos();
        assertNotNull(todos);
    }

    @Test
    void shouldFindAllPosts() {
        List<PostResponse> posts = JSONPlaceholderRestClient.findAllPosts();
        assertNotNull(posts);
        assertThat(posts).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldCreatePost() {
        PostRequest newPost = new PostRequest(5, null, "The Lord of the Rings", null);

        PostResponse post = JSONPlaceholderRestClient.createPost(newPost);
        assertNotNull(post);
        assertThat(post.userId()).isEqualTo(newPost.userId());
        assertThat(post.title()).isEqualTo(newPost.title());
    }

    @Test
    void shouldGetPostById() {
        PostResponse post = JSONPlaceholderRestClient.getPostById(5);
        assertNotNull(post);
    }

    @Test
    public void shouldGetAllPosts() {
        List<PostResponse> posts = declarativeJSONPlaceholderRestClient.getAllPosts();
        assertThat(posts.size()).isEqualTo(100);
    }

    @Test
    public void shouldGetSinglePost() {
        PostResponse post = declarativeJSONPlaceholderRestClient.getPost(5);
        assertThat(post.id()).isEqualTo(5);
    }

    @Test
    public void shouldDeleteSinglePost() {
        declarativeJSONPlaceholderRestClient.deletePost(5);
    }

    @Test
    public void shouldCreateNewPost() {
        PostRequest newPost = new PostRequest(1, null, "new title", "new body");
        PostResponse post = declarativeJSONPlaceholderRestClient.createPost(newPost);
        assertThat("new title").isEqualTo(post.title());
    }
}
