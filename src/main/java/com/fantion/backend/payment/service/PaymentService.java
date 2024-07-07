package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.ResponseDto.Success;
import com.fantion.backend.payment.dto.ResponseDto.fail;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

  PaymentDto.Response requestPayment(PaymentDto.Request request);

  Success successPayment(String orderId, String paymentKey, Long amount);

  fail failPayment(String code, String message, String orderId);

  CalcelDto allCancelPayment(String orderId, Request request);
}
