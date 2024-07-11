package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "PASSWORD_INVALID";
  }

  @Override
  public String getMessage() {
    return "잘못된 비밀번호 입니다.";
  }
}
