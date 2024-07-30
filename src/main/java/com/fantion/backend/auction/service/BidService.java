package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.*;
import com.fantion.backend.common.dto.ResultDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BidService {

    // 입찰
    ResultDTO<BidDto.Response> createBid(BidDto.Request request);

    // 입찰내역 구독
    SseEmitter subscribeBid(Long auctionId);

    // 입찰내역 발행
    void publishBid(BidDto.Response bid);

    // 입찰 마감 (낙찰 및 유찰)
    void finishBid();

    // 사용 가능한 예치금 조회
    ResultDTO<BalanceCheckDto.Response> useBalanceCheck();

    // 즉시 구매
    ResultDTO<BuyNowDto.Response> buyNow(BuyNowDto.Request request);

    // 입찰 취소
    ResultDTO<BidCancelDto.Response> cancelBid(BidCancelDto.Request request);

    // 거래중인 경매 물품 조회
    ResultDTO<BidSuccessListDto.Response> successBidAuctionList();
    // 인계 확인
    ResultDTO<HandOverDto.Response> sendChk(HandOverDto.Request request);

    // 인수 확인
    ResultDTO<HandOverDto.Response> receiveChk(HandOverDto.Request request);

    // 구매 철회
    ResultDTO<BidAuctionCancelDto.Response> cancelBidAuction(BidAuctionCancelDto.Request request);
}
