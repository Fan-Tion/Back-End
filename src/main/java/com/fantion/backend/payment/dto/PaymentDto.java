package com.fantion.backend.payment.dto;

import com.fantion.backend.payment.entity.Payment;
import com.fantion.backend.type.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "토스 결제 요청을 할 때 필요한 정보")
  public static class PaymentRequest {
    @NotNull(message = "결제유형은 필수값 입니다.")
    @Schema(description = "결제유형", example = "카드")
    private PaymentType paymentType;

    @NotNull(message = "결제금액은 필수값 입니다.")
    @Schema(description = "결제금액", example = "100000")
    private Long amount;

    @NotBlank(message = "결제상품 이름은 필수값 입니다.")
    @Schema(description = "결제상품", example = "예치금 충전")
    private String orderName;
  }

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(description = "토스 결제 요청시 리턴되는 값")
  public static class PaymentResponse {
    @Schema(description = "결제유형")
    private PaymentType paymentType;
    @Schema(description = "결제금액")
    private Long amount;
    @Schema(description = "결제상품")
    private String orderName;
    @Schema(description = "주문번호")
    private String orderId;
    @Schema(description = "결제 고객 이메일")
    private String customerEmail;
    @Schema(description = "결제 고객 이름(닉네임)")
    private String customerName;
    @Schema(description = "성공 시 리다이렉트 될 URL")
    private String successUrl;
    @Schema(description = "실패 시 리다이렉트 될 URL")
    private String failUrl;
    @Schema(description = "결제 성공 YN")
    private Boolean successYn;
    @Schema(description = "결제 취소 YN")
    private Boolean cancelYn;
    @Schema(description = "결제가 이루어진 시간")
    private LocalDateTime paymentData;
  }

  public static PaymentDto.PaymentResponse of(Payment payment, String successUrl, String failUrl) {

    return PaymentResponse.builder()
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
