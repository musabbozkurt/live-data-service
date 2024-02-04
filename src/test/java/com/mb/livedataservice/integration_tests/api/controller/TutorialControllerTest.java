package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.data.model.Tutorial;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TutorialControllerTest extends BaseUnitTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1");

    @Autowired
    private TestRestTemplate restTemplate;

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
}