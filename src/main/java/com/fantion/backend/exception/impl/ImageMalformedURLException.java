package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageMalformedURLException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCode() {
    return "IMAGE_MALFORMED";
  }

  @Override
  public String getMessage() {
    return "잘못된 형식의 URL입니다.";
  }
}
