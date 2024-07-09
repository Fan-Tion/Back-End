package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "EMAIL_DUPLICATE";
  }

  @Override
  public String getMessage() {
    return "이미 가입한 이메일입니다.";
  }
}
