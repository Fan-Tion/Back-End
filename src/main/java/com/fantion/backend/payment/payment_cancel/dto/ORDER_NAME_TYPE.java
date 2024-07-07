package com.fantion.backend.payment.payment_cancel.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ORDER_NAME_TYPE {
	STYLE_FEEDBACK(""), CRDI_OR_PRODUCT_RECMD("");
	private final String name;
}
