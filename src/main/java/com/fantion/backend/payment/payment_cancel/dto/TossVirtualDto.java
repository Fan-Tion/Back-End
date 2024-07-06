package com.fantion.backend.payment.payment_cancel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TossVirtualDto {
	private String secret;
	private String status;
	private String orderId;
}
