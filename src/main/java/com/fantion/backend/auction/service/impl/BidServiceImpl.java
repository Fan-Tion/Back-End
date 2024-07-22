package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.*;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.auction.service.BidService;
import com.fantion.backend.auction.service.RedisMessageService;
import com.fantion.backend.auction.service.SseEmitterService;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
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
import java.util.List;
import java.util.Optional;

import static com.fantion.backend.exception.ErrorCode.*;

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
            throw new CustomException(ErrorCode.TOO_OLD_AUCTION);

        }

        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 사용 가능한 예치금
        Long canUseBalance = balanceCheck(member).getCanUseBalance();

        // 사용 가능한 예치금이 입찰가보다 더 적을 경우
        if (canUseBalance < request.getBidPrice()) {
            throw new CustomException(ErrorCode.NOT_ENOUGH_BALANCE);
        }

        // 공개 입찰인 경우
        if (auction.isAuctionType()) {
            // 상위 입찰 설정
            auction.topBid(request.getBidPrice(),member.getNickname());
        }

        // 입찰 생성
        Bid bid = Bid.builder()
                .auctionId(auction)
                .bidPrice(request.getBidPrice())
                .bidder(member)
                .createDate(LocalDateTime.now())
                .build();

        // 기존 입찰 수정
        if (request.getBidId() != null) {
            bid.setBidId(request.getBidId());
        }

        BidDto.Response response = BidDto.Response(bidRepository.save(bid));
        publishBid(response);

        return response;
    }

    // 사용 가능한 예치금 조회
    @Override
    public BalanceCheckDto.Response useBalanceCheck() {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        return balanceCheck(member);
    }

    // 사용 가능한 예치금 확인
    public BalanceCheckDto.Response balanceCheck(Member member) {
        // 사용자의 보유한 예치금 조회
        Money money = moneyRepository.findByMemberId(member.getMemberId())
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_MONEY));
        Long haveBalance = money.getBalance();

        // 공개 입찰 사용 가능한 예치금 계산
        // 해당 사용자가 상위 입찰인 진행중인 경매 물품 조회
        List<Auction> topBidAuctionList = auctionRepository.findByCurrentBidderAndStatus(member.getNickname(), true);

        // 공개 입찰내역들의 입찰가 합산 금액
        Long totalBidPrice = 0L;

        for (int i = 0; i < topBidAuctionList.size(); i++) {
            Auction topBidAuction = topBidAuctionList.get(i);
            totalBidPrice += topBidAuction.getCurrentBidPrice();
        }

        // 비공개 입찰 사용 가능한 예치금 계산
        // 진행중인 비공개 입찰 경매 조회
        Long totalPrivateBidPrice = 0L;
        List<Auction> privateAuctionList = auctionRepository.findByAuctionTypeAndStatus(false, true);
        for (int i = 0; i < privateAuctionList.size(); i++) {
            Auction privateAuction = privateAuctionList.get(i);

            // 해당 사용자가 진행중인 비공개 입찰 조회
            Optional<Bid> privateAuctionBidder = bidRepository.findByAuctionIdAndBidder(privateAuction,member);

            // 입찰이 있을경우 합산
            if (privateAuctionBidder.isPresent()) {
                totalPrivateBidPrice += privateAuctionBidder.get().getBidPrice();
            }
        }

        BalanceCheckDto.Response balanceCheckResponse = BalanceCheckDto.Response.builder()
                .totalBidPrice(totalBidPrice + totalPrivateBidPrice)
                .canUseBalance(haveBalance - totalBidPrice - totalPrivateBidPrice)
                .build();

        return balanceCheckResponse;
    }

    // 입찰내역 구독
    @Transactional
    @Override
    public SseEmitter subscribeBid(BidSubscribeDto.Request request) {
        String channel = String.valueOf(request.getAuctionId());
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
        // 진행중인 경매 전체 조회
        List<Auction> auctionList = auctionRepository.findByStatus(true);
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

                    // 상위 입찰 설정
                    auction.topBid(bid.getBidPrice(), bidder.getNickname());

                }

            }
        }
    }

    // 즉시 구매
    @Override
    public BuyNowDto.Response buyNow(BuyNowDto.Request request) {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 사용자가 보유한 예치금 조회
        Money money = moneyRepository.findByMemberId(member.getMemberId())
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_MONEY));

        // 즉시 구매하려는 경매 물품 조회
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_AUCTION));

        // 경매 종료일
        LocalDateTime endDate = auction.getEndDate();

        // 구매자의 보유한 예치금
        Long balance = money.getBalance();

        // 구매자의 사용 가능한 예치금
        Long canUseBalance = balanceCheck(member).getCanUseBalance();

        // 즉시 구매가
        Long buyNowPrice = auction.getBuyNowPrice();

        // 경매 종료일이 지난 경우 즉시구매 불가능
        if (LocalDateTime.now().isAfter(endDate)) {
            throw new CustomException(ErrorCode.TOO_OLD_AUCTION);

        }

        // 사용 가능한 예치금 보다 즉시 구매가가 더 클 경우
        if (canUseBalance < buyNowPrice) {
            throw new CustomException(ErrorCode.NOT_ENOUGH_BALANCE);
        }

        // 경매 마감 설정
        auction.setStatus(false);

        // 상위 입찰 설정
        auction.topBid(auction.getBuyNowPrice(),member.getNickname());

        BuyNowDto.Response response = BuyNowDto.Response.builder()
                .title(auction.getTitle())
                .buyNowPrice(buyNowPrice)
                .balance(balance - buyNowPrice)
                .build();

        return response;
    }
    @Transactional
    @Override
    public BidCancelDto.Response cancelBid(BidCancelDto.Request request) {
        // 입찰 취소하려는 입찰 조회
        Bid cancelBid = bidRepository.findById(request.getBidId())
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_BID));

        // 입찰 취소하려는 경매 물품 조회
        Auction cancelAuction = auctionRepository.findById(cancelBid.getAuctionId().getAuctionId())
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_AUCTION));

        // 비공개 입찰만 입찰 취소 가능
        if (cancelAuction.isAuctionType()) {
            throw new CustomException(NOT_PRIVATE_BID_CANCEL);

        }

        // 경매 종료일이 지난 경우 입찰취소 불가능
        if (LocalDateTime.now().isAfter(cancelAuction.getEndDate())) {
            throw new CustomException(ErrorCode.TOO_OLD_AUCTION);

        }

        // 입찰 취소
        bidRepository.delete(cancelBid);

        BidCancelDto.Response response = BidCancelDto.Response.builder()
                .auctionId(cancelAuction.getAuctionId())
                .cancelPrice(cancelBid.getBidPrice())
                .build();

        return response;
    }

    // 인계 확인
    @Transactional
    @Override
    public HandOverDto.Response sendChk(HandOverDto.Request request) {
        // 인계 확인할 경매 물품 조회
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_AUCTION));

        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member seller = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 종료된 경매여야 인계확인 가능
        if (auction.isStatus()) {
            throw new CustomException(NOT_FINISH_AUCTION);
        }

        // 판매자가 아닌 경우
        if (!seller.getMemberId().equals(auction.getMember().getMemberId())) {
            throw new CustomException(NOT_AUCTION_SELLER);
        }

        // 이미 인계 확인이 되어있는 경우
        if (auction.isSendChk()) {
            throw new CustomException(ALREADY_SEND_CHK);
        }

        // 인계 확인
        auction.sendChking(true);

        HandOverDto.Response response = HandOverDto.Response.builder()
                .auctionId(auction.getAuctionId())
                .balance(auction.getCurrentBidPrice())
                .createDate(LocalDateTime.now())
                .build();

        return response;
    }

    // 인수 확인
    @Transactional
    @Override
    public HandOverDto.Response receiveChk(HandOverDto.Request request) {
        // 인수 확인할 경매 물품 조회
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_AUCTION));

        // 인계 확인이 되어있지 않는 경우
        if (!auction.isSendChk()) {
            throw new CustomException(NOT_SEND_CHKING);
        }

        // 이미 인수 확인이 되어있는 경우
        if (auction.isReceiveChk()) {
            throw new CustomException(ALREADY_RECEIVE_CHK);
        }

        // 인수 확인
        auction.receiveChking(true);

        // 구매자 정보
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 구매자 조회
        Member buyer = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 구매자가 아닌 경우
        if (!buyer.getNickname().equals(auction.getCurrentBidder())) {
            throw new CustomException(NOT_AUCTION_BUYER);
        }

        // 구매자가 보유한 예치금 조회
        Money buyerMoney = moneyRepository.findByMemberId(buyer.getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));

        // 판매자 정보
        // 사용자 조회
        Member seller = memberRepository.findById(auction.getMember().getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 판매자가 보유한 예치금 조회
        Money sellerMoney = moneyRepository.findByMemberId(seller.getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));

        // 예치금 정산
        buyerMoney.successBid(auction.getCurrentBidPrice());
        sellerMoney.successSellBid(auction.getCurrentBidPrice());

        // 예치금 내역 생성
        BalanceHistory buyerHistory = BalanceHistory.builder()
                .memberId(buyer)
                .balance(auction.getCurrentBidPrice())
                .type(BalanceType.USE)
                .createDate(LocalDateTime.now())
                .build();

        BalanceHistory sellerHistory = BalanceHistory.builder()
                .memberId(seller)
                .balance(auction.getCurrentBidPrice())
                .type(BalanceType.CHARGING)
                .createDate(LocalDateTime.now())
                .build();

        // 예치금 내역 저장
        balanceHistoryRepository.save(buyerHistory);
        balanceHistoryRepository.save(sellerHistory);

        HandOverDto.Response response = HandOverDto.Response.builder()
                .auctionId(auction.getAuctionId())
                .balance(auction.getCurrentBidPrice())
                .createDate(buyerHistory.getCreateDate())
                .build();

        return response;
    }


}
