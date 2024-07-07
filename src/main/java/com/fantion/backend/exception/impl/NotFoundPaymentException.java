package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundPaymentException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getErrorCode() {
    return "NOT_FOUND_PAYMENT_INFO";
  }

  @Override
  public String getMessage() {
    return "거래정보를 서버에서 찾을 수 없습니다.";
  }
}
