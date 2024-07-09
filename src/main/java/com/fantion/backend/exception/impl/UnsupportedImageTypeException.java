package com.fantion.backend.exception.impl;

import com.fantion.backend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class UnsupportedImageTypeException extends AbstractException {
    @Override
    public HttpStatus getHttpStatus() {


        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getErrorCode() {
        return "UNSUPPORTED_IMAGE_TYPE";
    }

    @Override
    public String getMessage() {
        return "지원되지 않는 이미지 파일 형식입니다.";
    }
}
