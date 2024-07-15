package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.BidAbstractException;
import com.fantion.backend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class BidException extends BidAbstractException {

    private String message;
    private HttpStatus status;
    private ErrorCode errorCode;

    public BidException(ErrorCode errorCode) {
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
