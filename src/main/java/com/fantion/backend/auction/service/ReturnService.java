package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.auction.dto.ReturnDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ReturnService {
  ReturnDto createAuctionReturn(Long auctionId);

  ReturnDto updateAuctionReturn(Response response);

  ReturnDto deleteAuctionReturn(boolean deleteCheck);

  ReturnDto  getAuctionListReturn(String option, Page<Response> list);

  ReturnDto categoryReturn(String favorite, List<CategoryDto> favoriteAuctionCategory);
}
