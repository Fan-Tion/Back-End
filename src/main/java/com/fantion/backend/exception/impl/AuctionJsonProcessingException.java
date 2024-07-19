package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AuctionJsonProcessingException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCode() {
    return "Json_Processing_Error";
  }

  @Override
  public String getMessage() {
    return "JSON 변환을 실패했습니다.";
  }
}