package com.fantion.backend.payment.dto;

import com.fantion.backend.type.PaymentType;
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
  public static class Success {
    private String orderId;
    private String orderName;
    private PaymentType method;
    private String totalAmount;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private List<Cancel> cancels;
  }

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class Cancel {
    private String transactionKey;
    private String cancelReason;
    private int taxExemptionAmount;
    private String canceledAt;
    private int easyPayDiscountAmount;
    private String receiptKey;
    private int cancelAmount;
    private int taxFreeAmount;
    private int refundableAmount;
    private String cancelStatus;
    private String cancelRequestId;
  }

  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class fail {
    private String errorCode;
    private String message;
    private String orderId;
  }
}
