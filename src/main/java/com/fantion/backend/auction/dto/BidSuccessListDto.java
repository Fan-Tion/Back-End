package com.fantion.backend.auction.dto;

import com.fantion.backend.auction.entity.Auction;
import lombok.*;

import java.util.List;

public class BidSuccessListDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private List<Auction> buyList;          // 구매중인 경매물품
        private List<Auction> sellList;         // 판매중인 경매물품

    }
}
