package com.mb.livedataservice.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.retry.RetryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class RestResponseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        String errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.VALIDATION_ERROR.getCode(), errorMessages), HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleBaseException(BaseException ex) {
        log.debug("BaseException occurred: {}", ex.getErrorCode(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getErrorCode().getCode(), ex.getMessage()), ex.getErrorCode().getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull ErrorResponse> handleException(Exception ex) {
        log.debug("Exception occurred: {}", ExceptionUtils.getStackTrace(ex));
        if ("Service unavailable".equalsIgnoreCase(ex.getMessage())) {
            return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.SERVICE_UNAVAILABLE.getCode(), ex.getMessage()), LiveDataErrorCode.SERVICE_UNAVAILABLE.getHttpStatus());
        }
        return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.UNEXPECTED_ERROR.getCode(), ex.getMessage()), LiveDataErrorCode.UNEXPECTED_ERROR.getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<@NonNull ErrorResponse> handleRateLimitException(RequestNotPermitted ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.TOO_MANY_REQUESTS.getCode(), "Rate limit exceeded - Please try again later"), LiveDataErrorCode.TOO_MANY_REQUESTS.getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleCircuitBreakerException(CallNotPermittedException ex) {
        log.warn("Circuit breaker is open: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.CIRCUIT_BREAKER_OPEN.getCode(), "Service temporarily unavailable"), LiveDataErrorCode.CIRCUIT_BREAKER_OPEN.getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(RetryException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleRetryExhaustedException(RetryException ex) {
        log.error("All retry attempts failed: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.RETRY_EXHAUSTED.getCode(), "Service unavailable after retries"), LiveDataErrorCode.RETRY_EXHAUSTED.getHttpStatus());
    }
}
