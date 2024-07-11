package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class SuspendedMemberException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "SUSPENDED_MEMBER";
  }

  @Override
  public String getMessage() {
    return "계정정지 조치된 회원입니다.";
  }
}
