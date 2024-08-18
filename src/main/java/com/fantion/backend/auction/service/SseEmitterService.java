package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.BidDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {

    SseEmitter createEmitter(String emitterKey);
    void deleteEmitter(String emitterKey);
    void sendBidToClient(String emitterKey, BidDto.Response bid);
    void send(Object data, String emitterKey, SseEmitter sseEmitter);
    void subscribeToAuction(String auctionId,String memberId);
    void sendEventToAuction(String auctionId,Object data);
    void endSsemitter(String auctionId, String memberId);
}
