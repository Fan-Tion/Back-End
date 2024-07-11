package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import java.io.IOException;
import org.springframework.http.HttpStatus;

public class NotFoundMemberException extends AbstractException {

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }

  @Override
  public String getErrorCode() {
    return "MEMBER_NOT_FOUND";
  }

  @Override
  public String getMessage() {
    return "존재하지 않는 회원입니다.";
  }
}
