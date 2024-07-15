package com.fantion.backend.exception;

import org.springframework.http.HttpStatus;

public abstract class BidAbstractException extends RuntimeException{


    abstract public HttpStatus getStatusCode();

    abstract public ErrorCode getErrorCode();

    abstract public String getMessage();
}
