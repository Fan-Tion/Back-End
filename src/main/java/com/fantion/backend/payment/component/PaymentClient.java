package com.fantion.backend.payment.component;

import com.fantion.backend.payment.dto.CancelSeperateDto;
import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.ConfirmDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.ResponseDto.Success;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "paymentClient", url = "${payment.base-url}")
public interface PaymentClient {

  @PostMapping(value = "${payment.confirm-endpoint}", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<Success> confirmPayment(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestBody ConfirmDto confirmDto);

  // DB에서 paymentKey를 가져와서 어떻게 넣어야할지 고민해보기
  @PostMapping(value = "/{paymentKey}/allCancel")
  ResponseEntity<CancelDto.Response> cancelPayment(
      @RequestHeader("Authorization") String authorizationHeader,
      @RequestHeader("IdempotencyKey") String IdempotencyKey,
      @RequestBody CancelDto.Request.getCancelReason cancelReason);

  @PostMapping(value = "/{paymentKey}/seperateCancel")
  ResponseEntity<CancelDto.Response> allCancelPayment(
          @RequestHeader("Authorization") String authorizationHeader,
          @RequestHeader("IdempotencyKey") String IdempotencyKey,
          @RequestBody CancelDto.Request.getSeperateCancelReason cancelReason);

}
