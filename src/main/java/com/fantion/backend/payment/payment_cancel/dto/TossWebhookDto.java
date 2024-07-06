package com.fantion.backend.payment.payment_cancel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fantion.backend.payment.payment_cancel.entity.PaymentWebhook;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossWebhookDto {
	private Long paymentSeq;
	private String eventType;
	private TossWebhookDataDto data;

	public PaymentWebhook toEntity() {
		return PaymentWebhook.builder()
				.eventType(eventType)
				.paymentKey(data.getPaymentKey())
				.status(data.getStatus())
				.orderId(data.getOrderId())
				.build();
	}
}
