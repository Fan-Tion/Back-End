package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByAuctionIdOrderByBidPriceDesc(Auction auctionId);
    Optional<Bid> findByAuctionIdAndBidder(Auction auctionId, Member bidder);
    Page<Bid> findByBidder(Member bidder, Pageable pageable);
    Long countByAuctionId(Auction auctionId);

}
