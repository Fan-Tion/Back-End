package com.fantion.backend.auction.dto;

import lombok.*;

public class AuctionFavoriteDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private boolean favoriteChk;      // 찜 여부
        private String title;            // 경매 제목

    }
}
