package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.auction.service.BidService;
import com.fantion.backend.auction.service.RedisMessageService;
import com.fantion.backend.auction.service.SseEmitterService;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.BalanceHistory;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.Money;
import com.fantion.backend.member.repository.BalanceHistoryRepository;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.repository.MoneyRepository;
import com.fantion.backend.type.BalanceType;
import com.fantion.backend.type.BidStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final SseEmitterService sseEmitterService;
    private final RedisMessageService redisMessageService;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final MemberRepository memberRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final MoneyRepository moneyRepository;

    // 입찰
    @Transactional
    @Override
    public BidDto.Response createBid(BidDto.Request request) {
        // 입찰하려는 경매 조회
        Auction auction = auctionRepository.getReferenceById(request.getAuctionId());
        LocalDateTime endDate = auction.getEndDate();

        // 경매 종료일이 지난 경우 입찰 불가능
        if (LocalDateTime.now().isAfter(endDate)) {
            throw new RuntimeException();

        }

        // 로그인한 사용자 가져오기
        Long loginUserId = Long.valueOf(MemberAuthUtil.getLoginUserId());

        // 사용자 조회
        Member member = memberRepository.findById(loginUserId)
                .orElseThrow(()-> new RuntimeException());

        // 예치금 확인
        balanceCheck(request.getBidPrice(), member.getMemberId(), auction);

        // 입찰 생성
        Bid bid = Bid.builder()
                .auctionId(auction)
                .bidPrice(request.getBidPrice())
                .bidder(member)
                .createDate(LocalDateTime.now())
                .status(BidStatus.PROGRESS)
                .build();

        BidDto.Response response = BidDto.Response(bidRepository.save(bid));
        publishBid(response);

        return response;
    }

    // 예치금 확인
    public void balanceCheck(Long bidPrice, Long memberId, Auction auction) {
        // 사용자의 예치금 조회
        Money money = moneyRepository.findByMemberId(memberId)
                .orElseThrow(()-> new RuntimeException());

        Long balance = money.getBalance();

        // 사용자 입찰 내역 조회
        // - 사용자의 현재 진행중인 경매의 입찰내역에서 입찰하려는 경매 물품의 입찰 내역은 제외 후 조회
        List<Bid> bidList = bidRepository.findByBidderAndAuctionIdNotIn(memberId, auction.getAuctionId());

        // 입찰내역 Map
        Map<Long, Long> bidPriceMap = new HashMap<>();

        // 입찰내역들의 입찰가 합산 금액
        Long totalBidPrice = 0L;

        // 같은 경매물품에서 가장 높은 입찰가
        Long maxPrice = 0L;

        for (int i = 0; i < bidList.size(); i++) {
            Bid bid = bidList.get(i);
            Long auctionId = bid.getAuctionId().getAuctionId();

            // 첫 입찰가 세팅
            if (bidPriceMap.get(auctionId) == null) {
                maxPrice = bid.getBidPrice();
            } else {
                maxPrice =  Math.max(bidPriceMap.get(auctionId), bid.getBidPrice());
            }

            // 경매 물품 식별자를 Key , 그 경매 물품의 가장 높은 입찰가를 Value로 셋팅
            bidPriceMap.put(auctionId,maxPrice);
        }

        // 입찰내역들의 입찰가 합산
        for (Map.Entry<Long, Long> entry : bidPriceMap.entrySet()) {
            log.info("[key]: {} / [value]: {}" ,entry.getKey(), entry.getValue());
            totalBidPrice += entry.getValue();
        }

        // 현재 경매 물품의 입찰가 합산
        totalBidPrice += bidPrice;
        log.info("totalBidPrice : {}",totalBidPrice);

        // 예치금이 부족한 경우
        if (balance < totalBidPrice) {
            throw new RuntimeException();
        }
    }

    // 입찰내역 구독
    @Transactional
    @Override
    public SseEmitter subscribeBid(Long auctionId) {
        String channel = String.valueOf(auctionId);
        // SSE 통신 객체 생성
        SseEmitter sseEmitter = sseEmitterService.createEmitter(channel);

        // 더미데이터 전송
        sseEmitterService.send("ReceivedData", channel, sseEmitter);

        // 채널 구독
        redisMessageService.subscribe(channel);

        // 만료시 emitter, 구독한 채널 삭제
        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> {
            sseEmitterService.deleteEmitter(channel);
            redisMessageService.removeSubscribe(channel);
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

    // 입찰 마감 (낙찰 및 유찰)
    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    @Override
    public void finishBid() {
        // 매일 한번씩 종료일이 지난 경매물품이 있는지 확인후 경매 마감 설정
        // 경매 물품 전체 조회
        List<Auction> auctionList = auctionRepository.findAll();
        for (int i = 0; i < auctionList.size(); i++) {
            Auction auction = auctionList.get(i);
            LocalDateTime endDate = auction.getEndDate();

            // 종료일이 지난 경매 물품인 경우
            if (LocalDateTime.now().isAfter(endDate)) {
                // 해당 경매 물품의 가장 높은 입찰내역 조회
                Optional<Bid> OptionalBid = bidRepository.findByAuctionIdOrderByBidPriceDesc(auction);

                // 입찰가가 없는 경우 (유찰)
                if (OptionalBid.isEmpty()) {
                    auctionRepository.delete(auction);

                } else {
                    Bid bid = OptionalBid.get();
                    Member bidder = bid.getBidder();

                    // 경매 마감 설정
                    auction.setStatus(false);
                    bid.setStatus(BidStatus.FINISH);

                    // 입찰자를 통해 예치금 조회
                    Money money = moneyRepository.findByMemberId(bidder.getMemberId())
                            .orElseThrow(()-> new RuntimeException());

                    // 보유한 예치금에서 입찰한 금액만큼 차감
                    money.successBid(bid.getBidPrice());

                    // 예치금 내역 생성
                    BalanceHistory history = BalanceHistory.builder()
                            .memberId(bidder)
                            .balance(bid.getBidPrice())
                            .type(BalanceType.USE)
                            .build();

                    // 예치금 내역 저장
                    balanceHistoryRepository.save(history);

                }

            }
        }
    }

}
