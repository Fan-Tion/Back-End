package com.fantion.backend.payment.controller;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentResponseDto;
import com.fantion.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/request")
  public ResponseEntity<?> requestPayment(@Valid @RequestBody PaymentDto.Request request) {
    ResultDTO<PaymentDto.Response> result = paymentService.requestPayment(request);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/success")
  public ResponseEntity<?> successPayment(
      @RequestParam(value = "orderId") String orderId,
      @RequestParam(value = "paymentKey") String paymentKey,
      @RequestParam(value = "amount") Long amount) {
    ResultDTO<PaymentResponseDto.Success> result = paymentService.successPayment(orderId,
        paymentKey, amount);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/fail")
  public ResponseEntity<?> failPayment(
      @RequestParam(value = "code") String code,
      @RequestParam(value = "message") String message,
      @RequestParam(value = "orderId") String orderId) {
    ResultDTO<PaymentResponseDto.fail> result = paymentService.failPayment(code, message, orderId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/cancel")
  public ResponseEntity<?> cancelPayment(@RequestBody CancelDto cancelDto) {
    ResultDTO<PaymentResponseDto.Success> result = paymentService.cancelPayment(cancelDto);
    return ResponseEntity.ok(result);
  }
}
