package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByAuctionIdOrderByBidPriceDesc(Auction auctionId);
    Optional<Bid> findByAuctionIdAndBidder(Auction auctionId, Member bidder);


}
