package com.mb.livedataservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class RestResponseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.debug("BaseException occurred: {}", ex.getErrorCode(), ex);
        return new ResponseEntity<>(new ErrorResponse(ex.getErrorCode().getCode(), ex.getMessage()), ex.getErrorCode().getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.debug("Exception occurred: {}", ExceptionUtils.getStackTrace(ex));
        return new ResponseEntity<>(new ErrorResponse(LiveDataErrorCode.UNEXPECTED_ERROR.getCode(), ex.getMessage()), LiveDataErrorCode.UNEXPECTED_ERROR.getHttpStatus());
    }
}
