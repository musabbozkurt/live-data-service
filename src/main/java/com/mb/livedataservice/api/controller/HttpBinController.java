package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/httpbin")
public class HttpBinController {

    private final RestClient httpBinRestClient;

    @GetMapping("/get")
    public @Nullable String get() {
        return httpBinRestClient.get()
                .uri("/get")
                .retrieve()
                .body(String.class);
    }

    @GetMapping("/get/status/{code}")
    public ResponseEntity<String> getStatusCode(@PathVariable Integer code) {
        ResponseEntity<Void> response = httpBinRestClient.get()
                .uri("/status/{code}", code)
                .retrieve()
                .toBodilessEntity(); // defaultStatusHandler fires now

        return ResponseEntity.ok("Success: " + response.getStatusCode().value());
    }

    @GetMapping("/unstable")
    @Retryable(includes = BaseException.class, maxRetries = 3, multiplier = 2)
    public @Nullable String getUnstable() {
        log.info("Attempting to call /unstable...");
        return httpBinRestClient.get()
                .uri("/unstable")
                .retrieve()
                .body(String.class);
    }
}
