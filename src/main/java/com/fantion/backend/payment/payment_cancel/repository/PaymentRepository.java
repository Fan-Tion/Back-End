package com.fantion.backend.payment.payment_cancel.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fantion.backend.payment.payment_cancel.entity.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderId(String orderId);

	Optional<Payment> findByPaymentKey(String paymentKey);

	List<Payment> findAllByCustomerEmail(String email, Pageable pageable);

	Optional<Payment> findByReservationSeq(Long reservationSeq);

	Optional<Payment> findByCustomerEmailAndReservationSeq(String customerEmail, Long reservationSeq);
}
