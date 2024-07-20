package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByAuctionIdOrderByBidPriceDesc(Auction auctionId);


}
