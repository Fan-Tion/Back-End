package com.fantion.backend.auction.dto;

import lombok.*;

public class BalanceCheckDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private Long totalBidPrice;     // 입찰중인 예치금
        private Long canUseBalance;     // 사용 가능한 예치금

    }

}
