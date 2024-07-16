package com.fantion.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {


    // Member
    NOT_FOUND_MEMBER("존재하지 않는 회원입니다.", HttpStatus.BAD_REQUEST),

    // Money
    NOT_FOUND_MONEY("예치금이 존재하지 않습니다.", HttpStatus.BAD_REQUEST),

    // Auction
    NOT_FOUND_AUCTION("존재하지 않는 경매입니다", HttpStatus.BAD_REQUEST),
    TOO_OLD_AUCTION("종료일이 지난 경매입니다.", HttpStatus.BAD_REQUEST),

    // Bid
    NOT_ENOUGH_BALANCE("예치금이 부족합니다.",HttpStatus.BAD_REQUEST);


    String message;
    HttpStatus status;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.status = httpStatus;
    }
}
