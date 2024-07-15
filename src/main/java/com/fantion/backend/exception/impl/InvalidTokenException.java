package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.FORBIDDEN; // 403 에러를 반환하도록 수정
  }

  @Override
  public String getErrorCode() {
    return "TOKEN_INVALID";
  }

  @Override
  public String getMessage() {
    return "유효하지 않은 토큰입니다.";
  }
}

