package com.mb.livedataservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

public enum LiveDataErrorCode implements Serializable, ErrorCode {

    UNEXPECTED_ERROR(HttpStatus.BAD_REQUEST),
    UNKNOWN_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_VALUE(HttpStatus.BAD_REQUEST),
    SCORE_BOARD_NOT_FOUND(HttpStatus.NOT_FOUND),
    SCORE_BOARD_HAS_NOT_ENDED(HttpStatus.BAD_REQUEST),
    CANNOT_MAP_RESPONSE(HttpStatus.BAD_REQUEST);

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private String message;

    LiveDataErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return this.name();
    }

}
