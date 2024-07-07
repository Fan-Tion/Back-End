package com.fantion.backend.payment.payment_cancel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fantion.backend.payment.payment_cancel.entity.PaymentWebhook;

@Repository
public interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {
}
