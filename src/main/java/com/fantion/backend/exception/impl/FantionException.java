package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.FantionAbstractException;
import com.fantion.backend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class FantionException extends FantionAbstractException {

    private String message;
    private HttpStatus status;
    private ErrorCode errorCode;

    public FantionException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus();
    }

    @Override
    public HttpStatus getStatusCode() {
        return this.status;
    }

    @Override
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
