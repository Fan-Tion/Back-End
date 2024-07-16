package com.fantion.backend.auction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class BidSubscribeDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "경매 식별자는 필수 항목입니다.")
        private Long auctionId;          // 경매 식별자

    }

}
