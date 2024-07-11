package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidEmailException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "EMAIL_INVALID";
  }

  @Override
  public String getMessage() {
    return "유효하지 않는 이메일 입니다. 이메일을 다시 한번 확인해주세요.";
  }
}
