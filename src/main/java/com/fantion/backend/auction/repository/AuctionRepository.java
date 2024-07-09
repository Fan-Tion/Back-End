package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

  Page<Auction> findByTitleContaining(String keyword, Pageable pageable);
}
