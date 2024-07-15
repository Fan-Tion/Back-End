package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageInvalidPathException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "IMAGE_NOT_HAVE_PATH";
  }

  @Override
  public String getMessage() {
    return "잘못된 이미지 파일 경로입니다.";
  }
}
