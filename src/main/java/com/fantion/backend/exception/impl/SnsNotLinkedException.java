package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class SnsNotLinkedException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "SNS_NOT_LINKED";
  }

  @Override
  public String getMessage() {
    return "소셜계정을 연동하지 않은 회원입니다.";
  }
}
