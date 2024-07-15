package com.fantion.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {


    TOO_OLD_AUCTION("종료일이 지난 경매입니다.", HttpStatus.BAD_REQUEST);



    String message;
    HttpStatus status;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.status = httpStatus;
    }
}
