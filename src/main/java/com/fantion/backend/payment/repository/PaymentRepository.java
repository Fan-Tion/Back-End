package com.fantion.backend.payment.repository;

import com.fantion.backend.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByOrderId(String orderId);
}
