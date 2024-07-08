package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.dto.SearchDto;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface AuctionService {
  AuctionDto.Response createAuction(
      AuctionDto.Request request,
      List<MultipartFile> auctionImage);

  AuctionDto.Response updateAuction(
      Request request,
      List<MultipartFile> auctionImage,
      Long auctionId);

  boolean deleteAuction(Long auctionId);

  Page<Response> getList(int page);

  Page<Response> getSearchList(SearchDto searchDto);

  // 입찰
  BidDto.Response createBid(BidDto.Request request);

  Resource getImage(Path imagePath, HttpHeaders headers);
}
