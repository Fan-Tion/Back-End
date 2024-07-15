package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.ResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

  PaymentDto.Response requestPayment(PaymentDto.Request request);

  ResponseDto.Success successPayment(String orderId, String paymentKey, Long amount);

  ResponseDto.fail failPayment(String code, String message, String orderId);
}
