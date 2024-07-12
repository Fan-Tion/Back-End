package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageInternalServerException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCode() {
    return "IMAGE_INTERNAL_SERVER_ERROR";
  }

  @Override
  public String getMessage() {
    return "이미지 내부 서버 오류입니다.";
  }
}
