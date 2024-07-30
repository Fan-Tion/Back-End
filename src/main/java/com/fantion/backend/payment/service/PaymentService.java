package com.fantion.backend.payment.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

  ResultDTO<PaymentDto.PaymentResponse> requestPayment(PaymentDto.PaymentRequest request);

  ResultDTO<PaymentResponseDto.PaymentSuccess> successPayment(String orderId, String paymentKey, Long amount);

  ResultDTO<PaymentResponseDto.PaymentFail> failPayment(String code, String message, String orderId);

  ResultDTO<PaymentResponseDto.PaymentSuccess> cancelPayment(CancelDto cancelDto);
}
