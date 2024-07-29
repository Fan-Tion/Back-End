package com.fantion.backend.auction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class BidCancelDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "경매 식별자는 필수 항목입니다.")
        private Long auctionId;         // 경매 식별자


    }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private Long auctionId;          // 경매 식별자
        private Long cancelPrice;        // 취소된 입찰가

    }

}
