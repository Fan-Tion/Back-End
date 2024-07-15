package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AuctionHttpMessageNotReadableException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "Enum_Invalid_Format";
  }

  @Override
  public String getMessage() {
    return "경매 카테고리가 존재하지 않습니다.";
  }
}