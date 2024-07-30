package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.FavoriteAuction;
import com.fantion.backend.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteAuctionRepository extends JpaRepository<FavoriteAuction, Long> {

    Optional<FavoriteAuction> findByAuctionAndMember(Auction auctionId, Member memberId);
    Page<FavoriteAuction> findByMember(Member member, Pageable pageable);
}
