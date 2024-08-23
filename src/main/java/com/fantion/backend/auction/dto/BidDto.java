package com.fantion.backend.auction.dto;

import com.fantion.backend.auction.entity.Bid;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.*;


public class BidDto {
  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request{
    @NotBlank(message = "경매 식별자는 필수 항목입니다.")
    private Long auctionId;         // 경매 식별자

    @NotBlank(message = "입찰가는 필수 항목입니다.")
    private Long bidPrice;          // 입찰가

  }

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Response{

    private Long auctionId;          // 경매 식별자
    private Long bidPrice;              // 입찰가
    private String bidder;              // 입찰자

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createDate;   // 입찰한 시간
  }

  // Entity에서 Dto로 변환
  public static BidDto.Response Response (Bid bid){
    return Response.builder()
            .auctionId(bid.getAuctionId().getAuctionId())
            .bidPrice(bid.getBidPrice())
            .bidder(bid.getBidder().getNickname())
            .createDate(bid.getCreateDate())
            .build();
  }
}
