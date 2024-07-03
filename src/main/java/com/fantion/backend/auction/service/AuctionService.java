package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import org.springframework.stereotype.Service;

@Service
public interface AuctionService {
  AuctionDto.Response createAuction(AuctionDto.Request request);
}
