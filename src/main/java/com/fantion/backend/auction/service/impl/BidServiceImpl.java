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
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new RuntimeException());

        // 사용 가능한 예치금
        Long canUseBalance = balanceCheck(member);

        // 사용 가능한 예치금이 입찰가보다 더 적을 경우
        if (canUseBalance < request.getBidPrice()) {
            throw new RuntimeException();
        }

        // 상위 입찰 설정
        auction.topBid(request.getBidPrice(),member.getNickname());

        // 입찰 생성
        Bid bid = Bid.builder()
                .auctionId(auction)
                .bidPrice(request.getBidPrice())
                .bidder(member)
                .createDate(LocalDateTime.now())
                .build();

        BidDto.Response response = BidDto.Response(bidRepository.save(bid));
        publishBid(response);

        return response;
    }

    // 사용 가능한 예치금 조회
    @Override
    public Long useBalanceCheck() {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new RuntimeException());

        // 사용 가능한 예치금
        Long canUseBalance = balanceCheck(member);

        return canUseBalance;
    }

    // 사용 가능한 예치금 확인
    public Long balanceCheck(Member member) {
        // 사용자의 보유한 예치금 조회
        Money money = moneyRepository.findByMemberId(member.getMemberId())
                .orElseThrow(()-> new RuntimeException());
        Long haveBalance = money.getBalance();

        // 해당 사용자가 상위 입찰인 진행중인 경매 물품 조회
        List<Auction> topBidAuctionList = auctionRepository.findByCurrentBidderAndStatus(member.getNickname(), true);

        // 입찰내역들의 입찰가 합산 금액
        Long totalBidPrice = 0L;

        for (int i = 0; i < topBidAuctionList.size(); i++) {
            Auction topBidAuction = topBidAuctionList.get(i);
            totalBidPrice += topBidAuction.getCurrentBidPrice();
        }

        return haveBalance - totalBidPrice;
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
