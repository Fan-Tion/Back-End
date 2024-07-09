package com.fantion.backend.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class ErrorResponse {

  private HttpStatus status;
  private String errorCode;
  private String message;
}
