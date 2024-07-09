package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import org.springframework.stereotype.Service;

@Service
public interface AuctionService {
  AuctionDto.Response createAuction(AuctionDto.Request request);

  // 경매 상세보기
  AuctionDto.Response findAuction(Long auctionId);
}
