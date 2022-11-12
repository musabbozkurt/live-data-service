package com.sportradar.livedataservice.exception;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class BaseException extends RuntimeException implements Serializable {
    private ErrorCode errorCode;
    private String message;
    private ErrorDetail errorDetail;

    public BaseException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}