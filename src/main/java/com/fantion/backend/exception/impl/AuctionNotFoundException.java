package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AuctionNotFoundException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  @Override
  public String getErrorCode() {
    return "AUCTION_NOT_FOUND";
  }

  @Override
  public String getMessage() {
    return "경매가 존재하지 않습니다.";
  }
}
