package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.BidDto;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface AuctionService {
  AuctionDto.Response createAuction(
      AuctionDto.Request request,
      List<MultipartFile> auctionImage) throws IOException;

  AuctionDto.Response updateAuction(
      Request request,
      List<MultipartFile> auctionImage,
      Long auctionId) throws IOException;

  boolean deleteAuction(Long auctionId) throws IOException;

  // 입찰
  BidDto.Response createBid(BidDto.Request request);

}
