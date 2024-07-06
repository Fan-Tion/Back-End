package com.fantion.backend.payment.payment_cancel.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fantion.backend.payment.payment_cancel.entity.CancelPayment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {

	Optional<CancelPayment> findByPaymentKey(String orderId);

	List<CancelPayment> findAllByCustomerEmail(String memberEmail, Pageable pageable);
}
