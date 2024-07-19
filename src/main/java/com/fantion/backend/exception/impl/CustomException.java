package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import com.fantion.backend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class CustomException extends AbstractException {

  private String message;
  private HttpStatus status;
  private ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    this.errorCode = errorCode;
    this.message = errorCode.getMessage();
    this.status = errorCode.getStatus();
  }

  @Override
  public HttpStatus getHttpStatus() {
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
