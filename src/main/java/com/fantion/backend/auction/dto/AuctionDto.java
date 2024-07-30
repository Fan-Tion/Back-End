package com.fantion.backend.auction.dto;

import com.fantion.backend.type.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuctionDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {

    @NotBlank(message = "제목은 꼭 입력해야 합니다.")
    private String title;
    @NotNull(message = "카테고리는 꼭 입력해야 합니다.")
    private CategoryType category;
    @NotNull(message = "경매 방식은 꼭 입력해야 합니다.")
    private boolean auctionType;
    @NotBlank(message = "설명은 꼭 입력해야 합니다.")
    private String description;
    @NotNull(message = "시작 입찰가는 꼭 입력해야 합니다.")
    private Long currentBidPrice;
    @NotNull(message = "즉시 구매가는 꼭 입력해야 합니다.")
    private Long buyNowPrice;
    @NotNull(message = "종료일은 꼭 입력해야 합니다.")
    private LocalDate endDate;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private Long auctionId;
    private String title;
    private String auctionUserNickname;
    private CategoryType category;
    private boolean auctionType;
    private List<String> auctionImage;
    private String description;
    private Long currentBidPrice;
    private String currentBidder;
    private Long buyNowPrice;
    private Long favoriteCnt;
    private LocalDate createDate;
    private LocalDateTime endDate;
    private boolean status;
    private Integer rating;
    private Long BidCount;
  }
}
