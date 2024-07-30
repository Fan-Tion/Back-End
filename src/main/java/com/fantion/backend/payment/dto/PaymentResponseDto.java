package com.fantion.backend.payment.dto;

import com.fantion.backend.type.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PaymentResponseDto {

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(description = "토스 결제 성공시 리턴되는 값")
  public static class PaymentSuccess {
    @Schema(description = "주문번호")
    private String orderId;
    @Schema(description = "결제상품")
    private String orderName;
    @Schema(description = "결제수단")
    private PaymentType method;
    @Schema(description = "총 결제 금액")
    private String totalAmount;
    @Schema(description = "결제 처리 상태")
    private String status;
    @Schema(description = "결제가 일어난 날짜와 시간 정보")
    private String requestedAt;
    @Schema(description = "결제 승인이 일어난 날짜와 시간 정보")
    private String approvedAt;
    @Schema(description = "결제 취소 이력")
    private List<Cancel> cancels;
  }

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(description = "결제 취소 이력")
  public static class Cancel {
    @Schema(description = "취소 건의 키 값")
    private String transactionKey;
    @Schema(description = "결제를 취소한 이유")
    private String cancelReason;
    @Schema(description = "취소된 금액 중 과세 제외 금액(컵 보증금 등)")
    private int taxExemptionAmount;
    @Schema(description = "결제 취소가 일어난 날짜와 시간 정보")
    private String canceledAt;
    @Schema(description = "간편결제 서비스의 포인트, 쿠폰, 즉시할인과 같은 적립식 결제수단에서 취소된 금액")
    private int easyPayDiscountAmount;
    @Schema(description = "취소 건의 현금영수증 키 값")
    private String receiptKey;
    @Schema(description = "결제를 취소한 금액")
    private int cancelAmount;
    @Schema(description = "취소된 금액 중 면세 금액")
    private int taxFreeAmount;
    @Schema(description = "결제 취소 후 환불 가능한 잔액")
    private int refundableAmount;
    @Schema(description = "취소 상태")
    private String cancelStatus;
    @Schema(description = "취소 요청 ID")
    private String cancelRequestId;
  }

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(description = "토스 결제 실패시 리턴되는 값")
  public static class PaymentFail {
    @Schema(description = "에러 코드")
    private String errorCode;
    @Schema(description = "에러 메시지")
    private String message;
    @Schema(description = "주문번호")
    private String orderId;
  }
}
