package com.fantion.backend.exception;

import com.fantion.backend.exception.impl.SnsNotLinkedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
    ErrorResponse errorResponse = ErrorResponse.builder()
        .status(HttpStatus.BAD_REQUEST)
        .errorCode("VALIDATION_ERROR") // 일반적인 에러 코드 사용
        .message("Validation failed: " + e.getBindingResult().getFieldError()
            .getDefaultMessage()) // 첫 번째 오류 메시지 사용
        .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
  
  @ExceptionHandler(BidAbstractException.class)
  protected ResponseEntity<BidErrorResponse> bidCustomException(BidAbstractException e) {

    BidErrorResponse errorResponse = BidErrorResponse.builder()
            .status(e.getStatusCode())
            .errorCode(e.getErrorCode())
            .message(e.getMessage())
            .build();

    return new ResponseEntity<>(errorResponse, e.getStatusCode());
  }
}
