package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.repository.SseEmitterRepository;
import com.fantion.backend.auction.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

    private final SseEmitterRepository sseEmitterRepository;
    private static ConcurrentHashMap<String, HashSet<String>> auctionChannels = new ConcurrentHashMap<>();

    @Value("${sse.timeout}")
    private Long timeout;
    @Override
    public SseEmitter createEmitter(String emitterKey) {
        return sseEmitterRepository.save(emitterKey, new SseEmitter(timeout));
    }

    @Override
    public void deleteEmitter(String emitterKey) {
        sseEmitterRepository.deleteById(emitterKey);
    }

    @Override
    public void sendBidToClient(String emitterKey, BidDto.Response bid) {
        sseEmitterRepository.findById(emitterKey)
                .ifPresent(emitter -> send(bid, emitterKey, emitter));
    }

    @Override
    public void send(Object data, String emitterKey, SseEmitter sseEmitter) {
        try {
            log.info("send to client {}:[{}]", emitterKey, data);
            sseEmitter.send(SseEmitter.event()
                    .id(emitterKey)
                    .name("addBid")
                    .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            log.error("IOException | IllegalStateException is occurred. ", e);
            sseEmitterRepository.deleteById(emitterKey);
        }
    }


    // 경매 물품 ID별로 사용자 구독 관리
    @Override
    public void subscribeToAuction(String auctionId, String memberId) {
        log.info("subscribeToAuction");
        log.info("auctionId : {}, memberId : {}",auctionId,memberId);
        auctionChannels.computeIfAbsent(auctionId, k -> new HashSet<>()).add(memberId);
    }

    @Override
    public void endSsemitter(String auctionId, String memberId){
        HashSet<String> memberIds = auctionChannels.get(auctionId);
        memberIds.remove(memberId);

    }
    // 특정 경매 물품의 구독자들에게 이벤트 전파
    @Override
    public void sendEventToAuction(String auctionId,Object data) {
        log.info("sendEventToAuction!!");
        log.info("auctionId : {}, Object : {}",auctionId,data);
        HashSet<String> subscribers = auctionChannels.get(auctionId);
        if (subscribers != null) {
            subscribers.forEach(memberId -> {
                Optional<SseEmitter> memberSseEmitter = sseEmitterRepository.findById(memberId);
                if (memberSseEmitter.isPresent()) {
                    try {
                        memberSseEmitter.get().send(SseEmitter.event()
                                .id(memberId)
                                .name("addBid")
                                .data(data, MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        sseEmitterRepository.deleteById(memberId);
                    }
                }
            });
        }
    }

}
