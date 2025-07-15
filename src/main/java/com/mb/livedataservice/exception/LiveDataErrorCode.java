package com.mb.livedataservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
public enum LiveDataErrorCode implements Serializable, ErrorCode {

    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    UNKNOWN_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_VALUE(HttpStatus.BAD_REQUEST),
    SCORE_BOARD_NOT_FOUND(HttpStatus.NOT_FOUND),
    SCORE_BOARD_HAS_NOT_ENDED(HttpStatus.BAD_REQUEST),
    CANNOT_MAP_RESPONSE(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE),
    CIRCUIT_BREAKER_OPEN(HttpStatus.SERVICE_UNAVAILABLE),
    RETRY_EXHAUSTED(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus httpStatus;

    private String message;

    LiveDataErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return this.name();
    }
}
