package com.fantion.backend.payment.controller;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.exception.ErrorResponse;
import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentResponseDto;
import com.fantion.backend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@Tag(name = "Payment", description = "Payment Service API")
public class PaymentController {

  private final PaymentService paymentService;

  @Operation(summary = "토스 결제 요청", description = "토스 결제 요청 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "결제를 요청했습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/request")
  public ResponseEntity<ResultDTO<PaymentDto.PaymentResponse>> requestPayment(
      @RequestBody @Valid PaymentDto.PaymentRequest request) {
    ResultDTO<PaymentDto.PaymentResponse> result = paymentService.requestPayment(request);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "토스 결제 성공", description = "토스 결제를 성공 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "결제를 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "클라이언트와 서버의 거래정보가 다릅니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "거래정보를 서버에서 찾을 수 없습니다.<br>존재하지 않는 회원입니다.<br>예치금이 존재하지 않습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/success")
  public ResponseEntity<?> successPayment(
      @RequestParam(value = "orderId") String orderId,
      @RequestParam(value = "paymentKey") String paymentKey,
      @RequestParam(value = "amount") Long amount) {
    ResultDTO<PaymentResponseDto.PaymentSuccess> result = paymentService.successPayment(orderId,
        paymentKey, amount);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "토스 결제 실패", description = "토스 결제를 실패 할 때 사용하는 API")
  @ApiResponse(responseCode = "200", description = "결제를 실패했습니다.")
  @GetMapping("/fail")
  public ResponseEntity<?> failPayment(
      @RequestParam(value = "code") String code,
      @RequestParam(value = "message") String message,
      @RequestParam(value = "orderId") String orderId) {
    ResultDTO<PaymentResponseDto.PaymentFail> result = paymentService.failPayment(code, message, orderId);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "토스 결제 취소", description = "토스 결제 취소 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "결제 취소를 성공했습니다.<br>중복 결제 취소 요청입니다."),
      @ApiResponse(responseCode = "400", description = "클라이언트와 서버의 거래정보가 다릅니다.<br>예치금이 부족합니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.<br>거래정보를 서버에서 찾을 수 없습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/cancel")
  public ResponseEntity<ResultDTO<PaymentResponseDto.PaymentSuccess>> cancelPayment(@RequestBody CancelDto cancelDto) {
    ResultDTO<PaymentResponseDto.PaymentSuccess> result = paymentService.cancelPayment(cancelDto);
    return ResponseEntity.ok(result);
  }
}
