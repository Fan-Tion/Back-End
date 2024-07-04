package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ValidPaymentInfoException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getErrorCode() {
    return "VALID_PAYMENT_INFO_EXCEPTION";
  }

  @Override
  public String getMessage() {
    return "클라이언트와 서버의 거래정보가 다릅니다.";
  }
}
