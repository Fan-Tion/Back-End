package com.fantion.backend.payment.repository;

import com.fantion.backend.payment.domain.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByAccountIdAndExpiredDateAfter(Long accountId, Date date);
}