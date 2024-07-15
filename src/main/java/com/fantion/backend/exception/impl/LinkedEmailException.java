package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class LinkedEmailException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "LINKED_EMAIL_ERROR";
  }

  @Override
  public String getMessage() {
    return "다른 이메일과 소셜계정 연동한 Email 입니다.";
  }
}
