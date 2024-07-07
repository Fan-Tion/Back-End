package com.fantion.backend.payment.component;

import com.fantion.backend.payment.dto.ConfirmDto;
import com.fantion.backend.payment.dto.ResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "paymentClient", url = "${payment.base-url}")
public interface PaymentClient {

  @PostMapping(value = "${payment.confirm-endpoint}", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<ResponseDto.Success> confirmPayment(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestBody ConfirmDto confirmDto);
}
