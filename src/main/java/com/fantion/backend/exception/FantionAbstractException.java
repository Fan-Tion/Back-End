package com.fantion.backend.exception;

import org.springframework.http.HttpStatus;

public abstract class FantionAbstractException extends RuntimeException{


    abstract public HttpStatus getStatusCode();

    abstract public ErrorCode getErrorCode();

    abstract public String getMessage();
}
