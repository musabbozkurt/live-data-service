package com.mb.livedataservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

public interface ErrorCode extends Serializable {
    HttpStatus getHttpStatus();

    String getCode();
}