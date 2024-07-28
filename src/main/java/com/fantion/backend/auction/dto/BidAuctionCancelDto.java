package com.fantion.backend.auction.dto;

import lombok.*;

import java.time.LocalDateTime;

public class BidAuctionCancelDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        private Long auctionId;             // 경매 식별자

    }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private Long auctionId;             // 경매 식별자
        private Long balance;               // 예치금
        private LocalDateTime createDate;   // 취소일자
    }

}