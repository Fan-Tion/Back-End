package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.type.CategoryType;
import com.fantion.backend.type.SearchType;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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

  Page<Response> getSearchList(int page, SearchType searchOption, CategoryType categoryOption, String keyword);

  // 경매 상세보기
  AuctionDto.Response findAuction(Long auctionId);

  Resource getImage(Path imagePath, HttpHeaders headers);

  void endAuctionSaveOrUpdate(String value);

  Map<String, Integer> getAuctionDateValue();

  CategoryDto getFavoriteAuctionCategory(Map<String, Integer> map);

  CategoryDto getAllAuctionCategory();
}
