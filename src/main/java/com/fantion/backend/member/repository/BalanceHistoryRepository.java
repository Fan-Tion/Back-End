package com.fantion.backend.member.repository;

import com.fantion.backend.member.entity.BalanceHistory;
import com.fantion.backend.member.entity.Member;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, Long> {

  @Query("SELECT b FROM BalanceHistory b WHERE b.memberId = :memberId AND b.createDate >= :startDate ORDER BY b.createDate DESC")
  Page<BalanceHistory> findByMemberAndCreateDateAfter(@Param("memberId") Member memberId, @Param("startDate") LocalDateTime startDate, Pageable pageable);
}
