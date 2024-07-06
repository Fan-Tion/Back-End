package com.fantion.backend.payment.payment_cancel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResHandleFailDto {
	String errorCode;
	String errorMsg;
	String orderId;
}
