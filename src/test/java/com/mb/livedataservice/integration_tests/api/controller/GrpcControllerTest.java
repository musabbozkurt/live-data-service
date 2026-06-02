package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.request.ApiHelloRequest;
import com.mb.livedataservice.api.response.ApiHelloResponse;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TestcontainersConfiguration.class,
        properties = {
                "spring.grpc.server.port=9999",
                "spring.grpc.client.hello-service.address=localhost:9999"
        }
)
class GrpcControllerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    private static Stream<Arguments> provideNamesForSayHello() {
        return Stream.of(
                Arguments.of("World", null, "Hello ==> World"),
                Arguments.of("World", "Hi", "Hi ==> World"),
                Arguments.of("Test", "", "Hello ==> Test"),
                Arguments.of("世界", "你好", "你好 ==> 世界"),
                Arguments.of("Test!@#$%", null, "Hello ==> Test!@#$%")
        );
    }

    private static Stream<Arguments> provideInvalidNamesForSayHello() {
        return Stream.of(
                Arguments.of("error-test", null, "name starting with 'error'"),
                Arguments.of("internal-test", null, "name starting with 'internal'"),
                Arguments.of("", null, "blank name")
        );
    }

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:%d".formatted(port))
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @MethodSource("provideNamesForSayHello")
    @ParameterizedTest(name = "sayHello with name=''{0}'' and greeting=''{1}'' should return ''{2}''")
    void sayHello_ShouldReturnGreeting_WhenValidNameProvided(String name, String greeting, String expectedMessage) {
        // Arrange
        ApiHelloRequest request = new ApiHelloRequest(name, greeting);

        // Act
        // Assertions
        webTestClient.post()
                .uri("/grpc/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiHelloResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(expectedMessage, response.message());
                });
    }

    @SuppressWarnings("unused") // description is used in @ParameterizedTest name
    @MethodSource("provideInvalidNamesForSayHello")
    @ParameterizedTest(name = "sayHello should return bad request for {2}")
    void sayHello_ShouldReturnBadRequest_WhenInvalidNameProvided(String name, String greeting, String description) {
        // Arrange
        ApiHelloRequest request = new ApiHelloRequest(name, greeting);

        // Act
        // Assertions
        webTestClient.post()
                .uri("/grpc/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void streamHello_ShouldReturnMultipleResponses_WhenValidNameProvided() {
        // Arrange
        ApiHelloRequest request = new ApiHelloRequest("StreamTest", null);

        // Act
        List<ApiHelloResponse> responses = webTestClient.post()
                .uri("/grpc/hello/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ApiHelloResponse.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(30));

        // Assertions
        assertNotNull(responses);
        assertEquals(10, responses.size());
        assertAll("All stream responses should match expected messages",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("Hello(%d) ==> StreamTest".formatted(i), responses.get(i).message(), "Response at index %d should match".formatted(i)))
        );
    }

    @Test
    void streamHello_ShouldReturnResponsesInOrder() {
        // Arrange
        ApiHelloRequest request = new ApiHelloRequest("OrderTest", null);

        // Act
        List<ApiHelloResponse> responses = webTestClient.post()
                .uri("/grpc/hello/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ApiHelloResponse.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(30));

        // Assertions
        assertNotNull(responses);
        assertEquals(10, responses.size());
        assertAll("All stream responses should be in order",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertTrue(responses.get(i).message().contains("(%d)".formatted(i)), "Response at index %d should contain correct order".formatted(i)))
        );
    }

    @Test
    void streamHello_ShouldReturnResponsesWithCustomGreeting() {
        // Arrange
        ApiHelloRequest request = new ApiHelloRequest("StreamTest", "Hola");

        // Act
        List<ApiHelloResponse> responses = webTestClient.post()
                .uri("/grpc/hello/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ApiHelloResponse.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(30));

        // Assertions
        assertNotNull(responses);
        assertEquals(10, responses.size());
        assertAll("All stream responses should use custom greeting",
                IntStream.range(0, 10)
                        .mapToObj(i -> () -> assertEquals("Hola(%d) ==> StreamTest".formatted(i), responses.get(i).message(), "Response at index %d should match".formatted(i)))
        );
    }

    @Test
    void sayHello_ShouldReturnGreeting_WhenLongNameProvided() {
        // Arrange
        String longName = "A".repeat(1000);
        ApiHelloRequest request = new ApiHelloRequest(longName, null);

        // Act
        // Assertions
        webTestClient.post()
                .uri("/grpc/hello")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiHelloResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("Hello ==> %s".formatted(longName), response.message());
                });
    }
}
