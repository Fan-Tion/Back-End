package com.fantion.backend.common.service.impl;

import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.common.dto.ReturnDto;
import com.fantion.backend.common.service.ReturnService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ReturnServiceImpl implements ReturnService {
  private static Map<String, Object> data;

  @Override
  public ReturnDto createAuctionReturn(Long auctionId) {
    Map<String, Object> data = new HashMap<>();
    data.put("success", true);
    data.put("auctionId", auctionId);

    return getReturnDto("성공적으로 경매를 생성했습니다.", data);
  }

  @Override
  public ReturnDto updateAuctionReturn(Response response) {
    Map<String, Object> data = new HashMap<>();
    data.put("success", true);
    data.put("title", response.getTitle());
    data.put("nickname", response.getAuctionUserNickname());

    return getReturnDto("성공적으로 경매를 변경했습니다.", data);
  }

  @Override
  public ReturnDto deleteAuctionReturn(boolean deleteCheck) {
    Map<String, Object> data = new HashMap<>();
    data.put("success", true);

    return getReturnDto("성공적으로 경매를 삭제했습니다.", data);
  }

  @Override
  public ReturnDto getAuctionListReturn(String option, Page<Response> list) {
    Map<String, Object> data = new HashMap<>();
    data.put("success", true);
    data.put("auctionList", list);

    if(option.equals("ALL")) {
      return getReturnDto("성공적으로 경매 리스트를 가져왔습니다.", data);
    } else if(option.equals("SEARCH")) {
      return getReturnDto("검색을 완료했습니다.", data);
    } else {
      // 에러는 생각해보기
      throw new RuntimeException("아직 exception 안정했음");
    }
  }

  @Override
  public ReturnDto categoryReturn(String option, List<CategoryDto> favoriteAuctionCategory) {
    Map<String, Object> data = new HashMap<>();
    data.put("success", true);
    data.put("categoryList", favoriteAuctionCategory);

    if(option.equals("ALL")) {
      return getReturnDto("성공적으로 카테고리를 가져왔습니다.", data);
    } else if(option.equals("FAVORITE")) {
      return getReturnDto("성공적으로 인기카테고리를 가져왔습니다.", data);
    } else {
      // 에러는 생각해보기
      throw new RuntimeException("아직 exception 안정했음");
    }
  }

  private static ReturnDto getReturnDto(String message, Map<String, Object> data) {
    ReturnDto returnDto = new ReturnDto();
    returnDto.setMessage(message);
    returnDto.setData(data);
    return returnDto;
  }
}
