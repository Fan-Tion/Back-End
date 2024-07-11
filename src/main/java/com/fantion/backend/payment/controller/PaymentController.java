package com.fantion.backend.payment.controller;

import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.ResponseDto;
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
  public ResponseEntity<PaymentDto.Response> requestPayment(
      @Valid @RequestBody PaymentDto.Request request) {
    PaymentDto.Response result = paymentService.requestPayment(request);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/success")
  public ResponseEntity<ResponseDto.Success> successPayment(@RequestParam String orderId,
      @RequestParam String paymentKey,
      @RequestParam Long amount) {
    ResponseDto.Success result = paymentService.successPayment(orderId, paymentKey, amount);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/fail")
  public ResponseEntity<ResponseDto.fail> failPayment(@RequestParam String code,
      @RequestParam String message,
      @RequestParam String orderId) {
    ResponseDto.fail result = paymentService.failPayment(code, message, orderId);
    return ResponseEntity.ok(result);
  }
}
