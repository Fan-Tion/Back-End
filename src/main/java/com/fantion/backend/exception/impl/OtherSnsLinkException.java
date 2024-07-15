package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class OtherSnsLinkException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "OTHER_SNS_LINK";
  }

  @Override
  public String getMessage() {
    return "이미 다른 소셜계정으로 연동 하셨습니다.";
  }
}
