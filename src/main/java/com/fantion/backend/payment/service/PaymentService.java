package com.fantion.backend.payment.service;

import com.fantion.backend.payment.dto.PaymentDto;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

  PaymentDto.Response requestPayment(PaymentDto.Request request);
}