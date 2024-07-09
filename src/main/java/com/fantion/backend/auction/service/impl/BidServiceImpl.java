package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.auction.service.BidService;
import com.fantion.backend.auction.service.RedisMessageService;
import com.fantion.backend.auction.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final SseEmitterService sseEmitterService;
    private final RedisMessageService redisMessageService;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    // 입찰
    @Transactional
    @Override
    public BidDto.Response createBid(BidDto.Request request) {
        // 입찰하려는 경매 조회
        Auction auction = auctionRepository.getReferenceById(request.getAuctionId());

        // 입찰 생성
        Bid bid = Bid.builder()
                .auctionId(auction)
                .bidPrice(request.getBidPrice())
                .bidder("tester")
                .createDate(LocalDateTime.now())
                .build();


        BidDto.Response response = BidDto.Response(bidRepository.save(bid));
        publishBid(response);


        return response;
    }
    // 입찰내역 구독
    @Transactional
    @Override
    public SseEmitter subscribeBid() {
        String memberKey = "1";
        // SSE 통신 객체 생성
        SseEmitter sseEmitter = sseEmitterService.createEmitter(memberKey);

        // 더미데이터 전송
        sseEmitterService.send("ReceivedData", memberKey, sseEmitter);

        // 채널 구독
        redisMessageService.subscribe(memberKey);

        // 만료시 emitter, 구독한 채널 삭제
        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> {
            sseEmitterService.deleteEmitter(memberKey);
            redisMessageService.removeSubscribe(memberKey);
        });

        return sseEmitter;
    }

    // 입찰내역 발행
    @Transactional
    @Override
    public void publishBid(BidDto.Response bid) {
        // redis 이벤트 발행
        redisMessageService.publish(String.valueOf(bid.getAuctionId()), bid);
    }


}
