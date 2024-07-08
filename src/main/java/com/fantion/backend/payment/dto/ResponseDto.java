package com.fantion.backend.payment.dto;

import com.fantion.backend.type.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ResponseDto {

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
