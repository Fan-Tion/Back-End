package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.member.entity.Member;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BidService {

    // 입찰
    BidDto.Response createBid(BidDto.Request request);

    // 입찰내역 구독
    SseEmitter subscribeBid(Long auctionId);

    // 입찰내역 발행
    void publishBid(BidDto.Response bid);

    // 입찰 마감 (낙찰 및 유찰)
    void finishBid();

    // 사용 가능한 예치금 조회
    Long useBalanceCheck();

}
