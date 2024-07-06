package com.fantion.backend.payment.payment_cancel.base.exception;

import com.fantion.backend.payment.payment_cancel.base.advice.ExMessage;

public class BussinessException extends RuntimeException {

	public BussinessException(ExMessage exMessage) {
		super(exMessage.getMessage());
	}

	public BussinessException(String message) {
		super(message);
	}
}

