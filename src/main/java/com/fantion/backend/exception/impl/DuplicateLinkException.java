package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class DuplicateLinkException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "DUPLICATE_LINK";
  }

  @Override
  public String getMessage() {
    return "이미 해당 소셜계정과 연동하셨습니다.";
  }
}
