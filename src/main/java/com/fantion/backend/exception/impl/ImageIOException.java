package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageIOException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCode() {
    return "IMAGE_IO_ERROR";
  }

  @Override
  public String getMessage() {
    return "파일이 없거나 접근할 수 없습니다.";
  }
}
