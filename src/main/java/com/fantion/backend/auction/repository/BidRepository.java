package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
