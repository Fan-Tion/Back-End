package com.fantion.backend.member.repository;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.RatingHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingHistoryRepository extends JpaRepository<RatingHistory, Long> {

  Optional<RatingHistory> findByAuctionIdAndMemberId(Long auctionId, Member buyer);
}
