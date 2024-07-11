package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageNotFoundException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCode() {
    return "IMAGE_NOT_FOUND";
  }

  @Override
  public String getMessage() {
    return "이미지가 존재하지 않습니다.";
  }
}
