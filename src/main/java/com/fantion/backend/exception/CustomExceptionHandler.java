package com.fantion.backend.exception;

import com.fantion.backend.exception.impl.TossApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
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

  @ExceptionHandler(TossAbstractException.class)
  protected ResponseEntity<TossErrorResponse> handleCustomException(TossAbstractException e) {
    TossErrorResponse errorResponse = TossErrorResponse.builder()
        .status(e.getHttpStatus())
        .errorCode(e.getErrorCode())
        .message(e.getMessage())
        .build();

    return new ResponseEntity<>(errorResponse, e.getHttpStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<TossErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
    TossErrorResponse errorResponse = TossErrorResponse.builder()
        .status(HttpStatus.BAD_REQUEST)
        .errorCode("VALIDATION_ERROR") // 일반적인 에러 코드 사용
        .message(e.getMessage())
        .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
