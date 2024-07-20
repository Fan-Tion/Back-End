package com.fantion.backend.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class TossErrorResponse {

  private HttpStatus status;
  private String errorCode;
  private String message;
}
