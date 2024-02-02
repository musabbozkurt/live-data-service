package com.mb.livedataservice.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
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