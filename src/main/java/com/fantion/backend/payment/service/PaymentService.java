package com.fantion.backend.payment.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

  ResultDTO<PaymentDto.Response> requestPayment(PaymentDto.Request request);

  ResultDTO<PaymentResponseDto.Success> successPayment(String orderId, String paymentKey, Long amount);

  ResultDTO<PaymentResponseDto.fail> failPayment(String code, String message, String orderId);

  ResultDTO<PaymentResponseDto.Success> cancelPayment(CancelDto cancelDto);
}
