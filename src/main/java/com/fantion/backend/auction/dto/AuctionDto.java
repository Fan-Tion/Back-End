package com.fantion.backend.auction.dto;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "경매 방식은 꼭 입력해야 합니다.")
    private boolean auctionType;
    //    @NotBlank(message = "이미지는 꼭 입력해야 합니다.")
    //    private blob auctionImage;
    @NotBlank(message = "설명은 꼭 입력해야 합니다.")
    private String description;
    @NotBlank(message = "시작 입찰가는 꼭 입력해야 합니다.")
    private Long currentBidPrice;
    @NotBlank(message = "즉시 구매가는 꼭 입력해야 합니다.")
    private Long buyNowPrice;
    @NotBlank(message = "종료일은 꼭 입력해야 합니다.")
    private LocalDateTime endDate;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {

    private String title;
    private String auctionUserNickname;
    private boolean auctionType;
    //    private blob auctionImage;
    private String description;
    private Long currentBidPrice;
    private String currentBidder;
    private Long buyNowPrice;
    private Long favoritePrice;
    private LocalDateTime createDate;
    private LocalDateTime endDate;
    private boolean status;
    private List<BidDto.Response> bidList;
  }
}
