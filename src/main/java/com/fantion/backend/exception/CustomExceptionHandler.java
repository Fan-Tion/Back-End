package com.fantion.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

  @ExceptionHandler(AbstractException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(AbstractException e) {

    ErrorResponse errorResponse = ErrorResponse.builder()
        .status(e.getHttpStatus())
        .errorCode(e.getErrorCode())
        .message(e.getMessage())
        .build();

    return new ResponseEntity<>(errorResponse, e.getHttpStatus());
  }
  @ExceptionHandler(FantionAbstractException.class)
  protected ResponseEntity<FantionErrorResponse> bidCustomException(FantionAbstractException e) {

    FantionErrorResponse errorResponse = FantionErrorResponse.builder()
            .status(e.getStatusCode())
            .errorCode(e.getErrorCode())
            .message(e.getMessage())
            .build();

    return new ResponseEntity<>(errorResponse, e.getStatusCode());
  }

}
