package com.fantion.backend.payment.component;

import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.ConfirmDto;
import com.fantion.backend.payment.dto.PaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "paymentClient", url = "${payment.base-url}")
public interface PaymentClient {

  @PostMapping(value = "${payment.confirm-endpoint}", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<PaymentResponseDto.PaymentSuccess> confirmPayment(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestBody ConfirmDto confirmDto);

  @PostMapping(value = "/{paymentKey}/${payment.cancel-endpoint}", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<PaymentResponseDto.PaymentSuccess> cancelPayment(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @PathVariable("paymentKey") String paymentKey,
      @RequestBody CancelDto cancelReason);

  @GetMapping(value = "/${payment.orders-endpoint}/{orderId}")
  ResponseEntity<PaymentResponseDto.PaymentSuccess> getPayment(
      @RequestHeader("Authorization") String authorizationHeader,
      @PathVariable("orderId") String orderId);
}
