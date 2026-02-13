package com.mb.livedataservice.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ErrorDetail errorDetail = null;
    private final transient ErrorCode errorCode;
    private final transient String message;

    public BaseException(String message) {
        super(message);
        this.errorCode = null;
        this.message = message;
    }

    public BaseException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}
