package com.fantion.backend.auction.dto;

import com.fantion.backend.auction.entity.Bid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


public class BidDto {
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request{
    @NotBlank(message = "경매 식별자는 필수 항목입니다.")
    private Long auctionId;          // 경매 식별자
    @NotBlank(message = "입찰가는 필수 항목입니다.")
    private Long bidPrice;          // 입찰가

  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response{

    private Long auctionId;          // 경매 식별자
    private Long bidPrice;              // 입찰가
    private String bidder;              // 입찰자
    private LocalDateTime createDate;   // 입찰한 시간

  }

  // Entity에서 Dto로 변환
  public static BidDto.Response Response (Bid bid){
    return Response.builder()
            .auctionId(bid.getAuctionId().getAuctionId())
            .bidPrice(bid.getBidPrice())
            .bidder(bid.getBidder())
            .createDate(bid.getCreateDate())
            .build();
  }
}
