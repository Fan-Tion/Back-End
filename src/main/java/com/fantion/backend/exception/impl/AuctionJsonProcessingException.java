package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import com.fantion.backend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AuctionJsonProcessingException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public ErrorCode getErrorCode() {
    return ErrorCode.IMAGE_INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getMessage() {
    return "JSON 변환을 실패했습니다.";
  }
}