package com.fantion.backend.payment.payment_cancel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TossErrorDto {
	String code;
	String message;
}
