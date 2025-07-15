package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.client.jsonplaceholder.DeclarativeJSONPlaceholderRestClient;
import com.mb.livedataservice.util.LiveDataConstants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class JsonPlaceholderController {

    private final DeclarativeJSONPlaceholderRestClient placeholderRestClient;

    @GetMapping("/retry/{id}")
    @Retry(name = LiveDataConstants.LIVE_DATA_SERVICE)
    public ResponseEntity<String> getPostWithRetry(@PathVariable Integer id) {
        log.info("Calling external service with retry for id: {}", id);
        return ResponseEntity.ok(callExternalService(id));
    }

    @GetMapping("/rate-limit/{id}")
    @RateLimiter(name = LiveDataConstants.LIVE_DATA_SERVICE)
    public ResponseEntity<String> getPostWithRateLimit(@PathVariable Integer id) {
        log.info("Calling external service with rate limiter for id: {}", id);
        return ResponseEntity.ok(callExternalService(id));
    }

    @GetMapping("/circuit-breaker/{id}")
    @CircuitBreaker(name = LiveDataConstants.LIVE_DATA_SERVICE, fallbackMethod = "fallbackForCircuitBreaker")
    public ResponseEntity<String> getPostWithCircuitBreaker(@PathVariable Integer id) {
        log.info("Calling external service with circuit breaker for id: {}", id);
        return ResponseEntity.ok(callExternalService(id));
    }

    @GetMapping("/all-patterns/{id}")
    @Retry(name = LiveDataConstants.LIVE_DATA_SERVICE)
    @RateLimiter(name = LiveDataConstants.LIVE_DATA_SERVICE)
    @CircuitBreaker(name = LiveDataConstants.LIVE_DATA_SERVICE, fallbackMethod = "fallbackForCircuitBreaker")
    public ResponseEntity<String> getPostWithAllPatterns(@PathVariable Integer id) {
        log.info("Calling external service with all patterns for id: {}", id);
        return ResponseEntity.ok(callExternalService(id));
    }

    private String callExternalService(Integer id) {
        return placeholderRestClient.getPost(id).toString();
    }

    public ResponseEntity<String> fallbackForCircuitBreaker(Integer id, Exception ex) {
        log.warn("Circuit breaker fallback triggered for id: {}, reason: {}", id, ex.getMessage());
        return ResponseEntity.ok("Fallback response for post " + id);
    }
}
