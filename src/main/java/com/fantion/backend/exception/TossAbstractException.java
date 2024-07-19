package com.fantion.backend.exception;

import org.springframework.http.HttpStatus;

public abstract class TossAbstractException extends RuntimeException {

  abstract public HttpStatus getHttpStatus();
  abstract public String getErrorCode();
  abstract public String getMessage();
}
