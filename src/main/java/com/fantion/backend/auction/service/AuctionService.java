package com.fantion.backend.auction.service;


import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.AuctionRequest;
import com.fantion.backend.auction.dto.AuctionFavoriteDto;
import com.fantion.backend.auction.dto.AuctionReportDto;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.type.CategoryType;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface AuctionService {

  ResultDTO<Map<String, Long>> createAuction(AuctionDto.AuctionRequest request,
      List<MultipartFile> auctionImage);

  ResultDTO<AuctionDto.AuctionResponse> updateAuction(AuctionRequest request,
      MultipartHttpServletRequest file, Long auctionId);

  ResultDTO<Boolean> deleteAuction(Long auctionId);

  ResultDTO<Page<AuctionDto.AuctionResponse>> getList(int page);

  ResultDTO<Page<AuctionDto.AuctionResponse>> getSearchList(int page, CategoryType category, String keyword);

  // 경매 상세보기
  ResultDTO<AuctionDto.AuctionResponse> findAuction(Long auctionId);

  Resource getImage(Path imagePath, HttpHeaders headers);

  void endAuctionSaveOrUpdate();

  Map<String, Integer> getAuctionDateValue();

  ResultDTO<List<CategoryDto>> getAllAuctionCategory();

  ResultDTO<List<CategoryDto>> getFavoriteAuctionCategory();

  // 찜 확인
  ResultDTO<AuctionFavoriteDto.Response> favoriteChk(Long auctionId);

  // 찜 & 찜 취소
  ResultDTO<AuctionFavoriteDto.Response> favoriteAuction(Long auctionId);

  ResultDTO<Page<AuctionDto.AuctionResponse>> getSellAuctionList(int page);

  ResultDTO<Page<AuctionDto.AuctionResponse>> getBuyAuctionList(int page);

  ResultDTO<Page<AuctionDto.AuctionResponse>> getJoinAuctionList(int page);

  ResultDTO<Page<AuctionDto.AuctionResponse>> getFavoriteAuctionList(int page);

  ResultDTO<AuctionReportDto.AuctionReportResponse> reportAuction(Long auctionId,
      AuctionReportDto.AuctionReportRequest request);
}
