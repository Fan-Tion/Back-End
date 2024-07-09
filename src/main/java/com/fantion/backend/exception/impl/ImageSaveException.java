package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ImageSaveException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "FAILED_IMAGE_SAVE";
  }

  @Override
  public String getMessage() {
    return "이미지 저장에 실패 했습니다.";
  }
}
