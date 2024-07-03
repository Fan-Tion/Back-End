package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.member.entity.Member;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {
  private final AuctionRepository auctionRepository;

  @Override
  public Response createAuction(Request request) {
    Auction auction = toAuction(request);

    auctionRepository.save(auction);

    return toResponse(auction);
  }

  private Auction toAuction(Request request) {
    return Auction.builder()
        .member(new Member())
        .title(request.getTitle())
        .auctionType(request.isAuctionType())
        .auctionImage(null)
        .description(request.getDescription())
        .currentBidPrice(request.getCurrentBidPrice())
        .currentBidder(null)
        .buyNowPrice(request.getBuyNowPrice())
        .favoriteCnt(0L)
        .createDate(LocalDateTime.now())
        .endDate(request.getEndDate())
        .status(true)
        .build();
  }

  private Response toResponse(Auction auction) {
    // 회원 닉네임 구현 필요

    return Response.builder()
        .title(auction.getTitle())
        .auctionUserNickname(null)
        .auctionType(auction.isAuctionType())
        .description(auction.getDescription())
        .currentBidPrice(auction.getCurrentBidPrice())
        .currentBidder(auction.getCurrentBidder())
        .buyNowPrice(auction.getBuyNowPrice())
        .favoritePrice(auction.getFavoriteCnt())
        .createDate(auction.getCreateDate())
        .endDate(auction.getEndDate())
        .status(auction.isStatus())
        .build();
  }
}
