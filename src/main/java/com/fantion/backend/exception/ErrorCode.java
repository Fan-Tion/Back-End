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
    UN_LINKED_ERROR("연동해제 중 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_DUPLICATE("기존과 동일한 비밀번호로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_RATED("이미 해당 경매건에 평점을 매겼습니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TIMEOUT("비밀번호 변경 기간이 만료되었습니다.", HttpStatus.BAD_REQUEST),

    // Payment
    NOT_FOUND_MONEY("예치금이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_PAYMENT_INFO("거래정보를 서버에서 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_INFO("클라이언트와 서버의 거래정보가 다릅니다.", HttpStatus.BAD_REQUEST),
    INVALID_CANCEL_AMOUNT("취소금액이 잘못되었습니다.", HttpStatus.BAD_REQUEST),

    // Auction
    NOT_FOUND_AUCTION("존재하지 않는 경매입니다.", HttpStatus.NOT_FOUND),
    TOO_OLD_AUCTION("종료일이 지난 경매입니다.", HttpStatus.BAD_REQUEST),
    ENUM_INVALID_FORMAT("경매 카테고리가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    AUCTION_NOT_FOUND("경매가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NOT_AUCTION_SELLER("판매자가 아닙니다.",HttpStatus.BAD_REQUEST),
    NOT_AUCTION_BUYER("구매자가 아닙니다.",HttpStatus.BAD_REQUEST),
    NOT_FINISH_AUCTION("종료된 경매가 아닙니다.",HttpStatus.BAD_REQUEST),
    NOT_FOUND_LINKED_EMAIL("연동한 이메일이 없습니다.", HttpStatus.NOT_FOUND),
    ALREADY_REPORT_AUCTION("이미 신고한 경매 입니다.", HttpStatus.BAD_REQUEST),

    // Bid
    NOT_FOUND_BID("존재하지 않는 입찰입니다.",HttpStatus.BAD_REQUEST),
    NOT_PRIVATE_BID_CANCEL("비공개 입찰만 입찰 취소가 가능합니다.",HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_BALANCE("예치금이 부족합니다.",HttpStatus.BAD_REQUEST),
    INVALID_BID_PRICE("잘못된 입찰가입니다.",HttpStatus.BAD_REQUEST),
    INVALID_BID("잘못된 입찰입니다",HttpStatus.BAD_REQUEST),
    NOT_SEND_CHKING("인계 확인이 되어있지 않습니다.",HttpStatus.BAD_REQUEST),
    ALREADY_CHKING("이미 인계 또는 인수 확인이 되어있습니다.",HttpStatus.BAD_REQUEST),
    ALREADY_CANCEL_CHK("이미 거래취소가 되어있습니다.",HttpStatus.BAD_REQUEST),
    NOT_CONFIRMED_RECEIVE("인수확인이 아직 완료되지 않았습니다.", HttpStatus.BAD_REQUEST),

    // Community
    NOT_FOUND_POST("존재하지 않은 게시물 입니다.", HttpStatus.NOT_FOUND),
    INVALID_POST_MEMBER("게시글의 작성자가 아닙니다.", HttpStatus.BAD_REQUEST),

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
    IMAGE_NOT_FOUND("이미지가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    SEND_MAIL_FAIL("메일전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_SEARCH_SCOPE("유효하지 않은 검색범위 입니다.", HttpStatus.BAD_REQUEST);


    String message;
    HttpStatus status;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.status = httpStatus;
    }
}
