package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.BidDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface BidService {

    // 입찰
    BidDto.Response createBid(BidDto.Request request);

    // 입찰내역 구독
    SseEmitter subscribeBid();

    // 입찰내역 발행
    void publishBid(BidDto.Response bid);


}
