package com.fantion.backend.payment.dto;

import com.fantion.backend.payment.entity.Payment;
import com.fantion.backend.type.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PaymentDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  public static class Request {
    @NotNull(message = "결제유형은 필수값 입니다.")
    private PaymentType paymentType;   // 결제 유형 (예: CARD)
    @NotNull(message = "결제금액은 필수값 입니다.")
    private Long amount;           // 결제 금액
    @NotBlank(message = "결제상품 이름은 필수값 입니다.")
    private String orderName;     // 주문 이름 (상품명)
  }

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Response {
    private PaymentType paymentType; // 결제 타입 - 카드/현금/포인트
    private Long amount; // 가격 정보
    private String orderName; // 주문명
    private String orderId; // 주문 Id
    private String customerEmail; // 고객 이메일
    private String customerName; // 고객 이름
    private String successUrl; // 성공 시 리다이렉트 될 URL
    private String failUrl; // 실패 시 리다이렉트 될 URL
    private Boolean successYn; // 결제 성공 YN
    private Boolean cancelYn; // 결제 취소 YN
    private LocalDateTime paymentData; // 결제가 이루어진 시간
  }

  public static PaymentDto.Response of(Payment payment, String successUrl, String failUrl) {

    return Response.builder()
        .paymentType(payment.getPaymentType())
        .amount(payment.getAmount())
        .orderName(payment.getOrderName())
        .orderId(payment.getOrderId())
        .customerEmail(payment.getMemberId().getEmail())
        .customerName(payment.getMemberId().getNickname())
        .successUrl(successUrl)
        .failUrl(failUrl)
        .successYn(false)
        .cancelYn(false)
        .paymentData(LocalDateTime.now())
        .build();
  }

}
