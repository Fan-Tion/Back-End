package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageSecurityException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.FORBIDDEN;
  }

  @Override
  public String getErrorCode() {
    return "IMAGE_ACCESS_DENIED";
  }

  @Override
  public String getMessage() {
    return "권한이 없어 접근이 불가능한 이미지입니다.";
  }
}
