package com.fantion.backend.auction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class HandOverDto {
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
        private LocalDateTime createDate;   // 인수인계 일자
    }
}
