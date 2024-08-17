package com.fantion.backend.auction.repository;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.CategoryType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

  Page<Auction> findByCategoryAndTitleContaining(CategoryType categoryType, String keyword, Pageable pageable);

  Page<Auction> findByCategory(CategoryType categoryType, Pageable pageable);

  List<Auction> findByCurrentBidderAndStatus(String currentBidder,boolean status);

  List<Auction> findByStatus(boolean status);
  
  List<Auction> findByEndDateAndStatus(LocalDate endDay, boolean auctionStatus);

  List<Auction> findByAuctionTypeAndStatus(boolean auctionType,boolean status);

  List<Auction> findByStatusAndReceiveChkAndCurrentBidderAndCancelChk(boolean status,boolean receiveChk,String bidder,boolean cancelChk);

  List<Auction> findByStatusAndReceiveChkAndMember(boolean status, boolean receiveChk, Member member);

  Page<Auction> findByMember(Member member,Pageable pageable);
  
  Page<Auction> findByStatusAndReceiveChkAndCurrentBidder(boolean status,boolean receiveChk,String bidder,Pageable pageable);
  
  List<Auction> findAllByCurrentBidder(String nickname);
  
  Optional<Auction> findTopByMemberOrderByAuctionIdDesc(Member member);

  Page<Auction> findAllByTitleContaining(String keyword, Pageable pageable);

  Page<Auction> findAllByStatus(boolean status, Pageable pageable);

  List<Auction> findAllByMemberId(Member member);
}
