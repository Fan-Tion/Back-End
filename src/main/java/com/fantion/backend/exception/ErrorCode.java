package com.fantion.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {


    // Member
    NOT_FOUND_MEMBER("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    EMAIL_INVALID("유효하지 않는 이메일 입니다.", HttpStatus.BAD_REQUEST),
    EMAIL_DUPLICATE("이미 가입한 이메일 입니다.", HttpStatus.BAD_REQUEST),
    LINKED_EMAIL_ERROR("다른 이메일과 소셜계정 연동한 이메일 입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_DUPLICATE("중복된 닉네임 입니다.", HttpStatus.BAD_REQUEST),
    NICKNAME_INVALID("닉네임이 유효하지 않습니다. 1~12글자의 한글, 영문 및 숫자로만 구성되어야 합니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID("잘못된 비밀번호 입니다.", HttpStatus.BAD_REQUEST),
    SUSPENDED_MEMBER("계정정지 조치된 회원입니다.", HttpStatus.BAD_REQUEST),
    OTHER_SNS_LINKED_ERROR("이미 다른 소셜계정으로 연동 하셨습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_LINKED_ERROR("이미 해당 소셜계정과 연동하셨습니다.", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID("유효하지 않은 토큰입니다.", HttpStatus.FORBIDDEN),

    // Payment
    NOT_FOUND_MONEY("예치금이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_PAYMENT_INFO("거래정보를 서버에서 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    VALID_PAYMENT_INFO("클라이언트와 서버의 거래정보가 다릅니다.", HttpStatus.BAD_REQUEST),

    // Auction
    NOT_FOUND_AUCTION("존재하지 않는 경매입니다", HttpStatus.BAD_REQUEST),
    TOO_OLD_AUCTION("종료일이 지난 경매입니다.", HttpStatus.BAD_REQUEST),
    ENUM_INVALID_FORMAT("경매 카테고리가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    AUCTION_NOT_FOUND("경매가 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // Bid
    NOT_ENOUGH_BALANCE("예치금이 부족합니다.",HttpStatus.BAD_REQUEST),

    // Common
    PARSING_ERROR("파싱 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    FAILED_IMAGE_SAVE("이미지 저장에 실패 했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UN_SUPPORTED_IMAGE_TYPE("지원되지 않는 이미지 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    IMAGE_IO_ERROR("파일이 없거나 접근할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_ACCESS_DENIED("권한이 없어 접근이 불가능한 이미지입니다.", HttpStatus.FORBIDDEN),
    IMAGE_NOT_HAVE_PATH("잘못된 이미지 파일 경로입니다.", HttpStatus.BAD_REQUEST),
    IMAGE_EXCEPTION("이미지 관련 에러입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_INTERNAL_SERVER_ERROR("이미지 내부 서버 오류입니다.",HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_MALFORMED("잘못된 형식의 URL입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_NOT_FOUND("이미지가 존재하지 않습니다.", HttpStatus.NOT_FOUND);


    String message;
    HttpStatus status;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.status = httpStatus;
    }
}
