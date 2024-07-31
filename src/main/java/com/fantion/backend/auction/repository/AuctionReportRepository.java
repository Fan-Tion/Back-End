package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.AuctionReport;
import com.fantion.backend.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionReportRepository extends JpaRepository<AuctionReport, Long> {

  Optional<AuctionReport> findByAuctionIdAndMemberId(Auction auction, Member member);
}
