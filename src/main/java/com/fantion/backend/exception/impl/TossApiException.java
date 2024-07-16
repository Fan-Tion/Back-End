package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.TossAbstractException;
import org.springframework.http.HttpStatus;

public class TossApiException extends TossAbstractException {

  private final HttpStatus status;
  private final String errorCode;
  private final String errorMessage;

  public TossApiException(HttpStatus status,String errorCode, String errorMessage) {
    this.status = status;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return status;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessage() {
    return errorMessage;
  }
}
