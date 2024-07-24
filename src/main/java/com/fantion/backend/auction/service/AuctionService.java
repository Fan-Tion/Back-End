package com.fantion.backend.auction.service;


import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.type.CategoryType;
import com.fantion.backend.type.SearchType;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

public interface AuctionService {
  ResultDTO<String> createAuction(
      AuctionDto.Request request,
      List<MultipartFile> auctionImage);

  ResultDTO<AuctionDto.Response> updateAuction(
      AuctionDto.Request request,
      List<MultipartFile> auctionImage,
      Long auctionId);

  ResultDTO<Boolean> deleteAuction(Long auctionId);

  ResultDTO<Page<AuctionDto.Response>> getList(int page);

  ResultDTO<Page<AuctionDto.Response>> getSearchList(int page, SearchType searchOption, CategoryType categoryOption, String keyword);

  // 경매 상세보기
  ResultDTO<AuctionDto.Response> findAuction(Long auctionId);

  Resource getImage(Path imagePath, HttpHeaders headers);

  void endAuctionSaveOrUpdate();

  Map<String, Integer> getAuctionDateValue();

  ResultDTO<List<CategoryDto>> getAllAuctionCategory();

  ResultDTO<List<CategoryDto>> getFavoriteAuctionCategory();
}
