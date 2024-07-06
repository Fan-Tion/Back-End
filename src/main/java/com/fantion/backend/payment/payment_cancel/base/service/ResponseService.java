package com.fantion.backend.payment.payment_cancel.base.service;

import org.springframework.stereotype.Service;
import com.fantion.backend.payment.payment_cancel.base.dto.CommonResponse;
import com.fantion.backend.payment.payment_cancel.base.dto.CommonResult;
import com.fantion.backend.payment.payment_cancel.base.dto.ListResult;
import com.fantion.backend.payment.payment_cancel.base.dto.SingleResult;

import java.util.List;

@Service
public class ResponseService {

    // 단일 결과
    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    // 복수 결과
    public <T> ListResult<T> getListResult(List<T> data) {
        ListResult<T> result = new ListResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    // 성공 결과
    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }

    // 실패 결과
    public CommonResult getFailResult(int code, String message) {
        CommonResult result = new CommonResult();
        setFailResult(result, code, message);
        return result;
    }

    // 성공 결과 매핑
    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMessage(CommonResponse.SUCCESS.getMessage());
    }

    // 실패 결과 매핑
    private void setFailResult(CommonResult result, int code, String message) {
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
    }
}
