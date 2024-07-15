package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class InvalidNicknameException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "INVALID_NICKNAME";
  }

  @Override
  public String getMessage() {
    return "닉네임이 유효하지 않습니다. 1~12글자의 한글, 영문 및 숫자로만 구성되어야 합니다.";
  }
}
