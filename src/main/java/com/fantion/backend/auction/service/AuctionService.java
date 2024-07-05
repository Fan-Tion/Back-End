package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.BidDto;
import org.springframework.stereotype.Service;

@Service
public interface AuctionService {
  AuctionDto.Response createAuction(AuctionDto.Request request);

  // 입찰
  BidDto.Response createBid(BidDto.Request request);
}
