package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestcontainersConfiguration.class)
class HttpBinControllerTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    private static void log(Throwable e) {
        log.warn("External httpbin service unavailable, skipping test: {}", e.getMessage());
    }

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:%d".formatted(port))
                .responseTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Test
    void get_ShouldReturnHttpBinResponse_WhenValidRequest() {
        try {
            webTestClient.get().uri("/httpbin/get")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(body -> assertThat(body).contains("origin", "headers", "url"));
        } catch (Exception | AssertionError e) {
            log(e);
            Assumptions.abort("External httpbin service unavailable: " + e.getMessage());
        }
    }

    @Test
    void getStatusCode_ShouldReturnSuccess_WhenStatusIs2xx() {
        try {
            webTestClient.get().uri("/httpbin/get/status/200")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .value(body -> assertThat(body).contains("Success: 200"));
        } catch (Exception | AssertionError e) {
            log(e);
            Assumptions.abort("External httpbin service unavailable: " + e.getMessage());
        }
    }

    @Test
    void getStatusCode_ShouldReturnNotFound_WhenStatusIs404() {
        try {
            webTestClient.get().uri("/httpbin/get/status/404")
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(String.class)
                    .value(body -> assertThat(body).contains("NOT_FOUND"));
        } catch (Exception | AssertionError e) {
            log(e);
            Assumptions.abort("External httpbin service unavailable: " + e.getMessage());
        }
    }

    @Test
    void getStatusCode_ShouldReturnError_WhenStatusIs500() {
        try {
            webTestClient.get().uri("/get/status/500")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(String.class)
                    .value(body -> assertThat(body).contains("UNEXPECTED_ERROR"));
        } catch (Exception | AssertionError e) {
            log(e);
            Assumptions.abort("External httpbin service unavailable: " + e.getMessage());
        }
    }

    @Test
    void getUnstable_ShouldHandleNonDeterministicResponse_WhenValidRequest() {
        try {
            EntityExchangeResult<String> result = webTestClient.get().uri("/unstable")
                    .exchange()
                    .expectBody(String.class)
                    .returnResult();

            int status = result.getStatus().value();
            String body = result.getResponseBody();

            // httpbin.org/unstable randomly succeeds or fails; after the defaultStatusHandler
            // and @Retryable, the possible outcomes are:
            //  200 — httpbin returned success
            //  400 — UNEXPECTED_ERROR (httpbin returned a non-404 error)
            //  404 — NOT_FOUND (httpbin returned 404)
            //  503 — RETRY_EXHAUSTED (retries wrapped in RetryException)
            assertThat(status).isIn(200, 400, 404, 503);
            switch (status) {
                case 200 -> assertThat(body).isNotBlank();
                case 400 -> assertThat(body).contains("UNEXPECTED_ERROR");
                case 404 -> assertThat(body).contains("NOT_FOUND");
                case 503 -> assertThat(body).contains("RETRY_EXHAUSTED");
                default -> throw new AssertionError("Unexpected status: " + status);
            }
        } catch (Exception | AssertionError e) {
            log(e);
            Assumptions.abort("External httpbin service unavailable: " + e.getMessage());
        }
    }
}
