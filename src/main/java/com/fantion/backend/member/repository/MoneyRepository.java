package com.fantion.backend.member.repository;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.Money;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoneyRepository extends JpaRepository<Money, Long> {

  Optional<Money> findByMemberId(Long memberId);
}
