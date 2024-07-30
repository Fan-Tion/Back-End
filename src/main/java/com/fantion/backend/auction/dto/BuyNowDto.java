package com.fantion.backend.auction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class BuyNowDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "경매 식별자는 필수 항목입니다.")
        private Long auctionId;          // 경매 식별자

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private String title;           // 경매 물품 제목
        private Long buyNowPrice;       // 즉시 구매가
        private Long balance;           // 남은 예치금

    }
}
