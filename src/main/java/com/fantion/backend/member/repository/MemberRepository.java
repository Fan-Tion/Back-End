package com.fantion.backend.member.repository;

import com.fantion.backend.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findById(Long id);

  Optional<Member> findByEmail(String email);

  Optional<Member> findByNickname(String nickname);

  Optional<Member> findByLinkedEmail(String LinkedEmail);

  List<Member> findAllByWithdrawalDateBefore(LocalDateTime thirtyDaysAgo);
}
