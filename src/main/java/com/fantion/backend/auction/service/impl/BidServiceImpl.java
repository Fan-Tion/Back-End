package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.*;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.auction.service.BidService;
import com.fantion.backend.auction.service.RedisMessageService;
import com.fantion.backend.auction.service.SseEmitterService;
import com.fantion.backend.common.dto.ResultDTO;
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
    public ResultDTO<BidDto.Response> createBid(BidDto.Request request) {
        // 입찰하려는 경매 조회
        Auction auction = auctionRepository.getReferenceById(request.getAuctionId());

        // 경매 마감인 경우 입찰 불가능
        if (!auction.isStatus()) {
            throw new CustomException(TOO_OLD_AUCTION);

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
            throw new CustomException(NOT_ENOUGH_BALANCE);
        }

        // 공개 입찰인 경우
        if (auction.isAuctionType()) {
            // 상위 입찰 설정
            auction.topBid(request.getBidPrice(),member.getNickname());
        }

        // 기존에 입찰한 경매물품 조회
        Optional<Bid> bidingAuction = bidRepository.findByAuctionIdAndBidder(auction, member);

        // 응답값 변수
        BidDto.Response response;

        // 기존에 입찰한 경매물품이 아닌 경우
        if (bidingAuction.isEmpty()) {
            // 입찰 생성
            Bid newBid = Bid.builder()
                    .auctionId(auction)
                    .bidPrice(request.getBidPrice())
                    .bidder(member)
                    .createDate(LocalDateTime.now())
                    .build();
            response = BidDto.Response(bidRepository.save(newBid));
        } else {
            // 기존 입찰 갱신
            Bid oldBid = bidingAuction.get();
            oldBid.updateBid(request.getBidPrice(),LocalDateTime.now());
            response = BidDto.Response(oldBid);
        }

        publishBid(response);
        return ResultDTO.of("성공적으로 입찰되었습니다.", response);
    }

    // 사용 가능한 예치금 조회
    @Override
    public ResultDTO<BalanceCheckDto.Response> useBalanceCheck() {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        return ResultDTO.of("성공적으로 사용 가능한 예치금이 조회되었습니다.", balanceCheck(member));
    }

    // 사용 가능한 예치금 확인
    public BalanceCheckDto.Response balanceCheck(Member member) {
        // 사용자의 보유한 예치금 조회
        Money money = moneyRepository.findByMemberId(member.getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));
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
                    Member buyer = bid.getBidder();

                    // 경매 마감 설정
                    auction.setStatus(false);

                    // 상위 입찰 설정 (비공개 입찰)
                    auction.topBid(bid.getBidPrice(), buyer.getNickname());

                    // 구매자(낙찰자) 예치금 조회
                    Money buyerMoney = moneyRepository.findByMemberId(buyer.getMemberId())
                            .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));

                    // 구매자가 보유한 예치금에서 낙찰된 금액만큼 차감
                    buyerMoney.useBalance(bid.getBidPrice());

                    // 예치금 내역 생성
                    BalanceHistory buyerHistory = BalanceHistory.builder()
                            .memberId(buyer)
                            .balance(bid.getBidPrice())
                            .type(BalanceType.USE)
                            .build();

                    // 예치금 내역 저장
                    balanceHistoryRepository.save(buyerHistory);

                }

            }
        }
    }

    // 즉시 구매
    @Transactional
    @Override
    public ResultDTO<BuyNowDto.Response> buyNow(BuyNowDto.Request request) {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member buyer = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 사용자가 보유한 예치금 조회
        Money buyerMoney = moneyRepository.findByMemberId(buyer.getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));

        // 즉시 구매하려는 경매 물품 조회
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_AUCTION));

        // 구매자의 사용 가능한 예치금
        Long buyerCanUseBalance = balanceCheck(buyer).getCanUseBalance();

        // 즉시 구매가
        Long buyNowPrice = auction.getBuyNowPrice();

        // 경매 마감인 경우 즉시구매 불가능
        if (!auction.isStatus()) {
            throw new CustomException(TOO_OLD_AUCTION);

        }

        // 사용 가능한 예치금 보다 즉시 구매가가 더 클 경우
        if (buyerCanUseBalance < buyNowPrice) {
            throw new CustomException(NOT_ENOUGH_BALANCE);
        }

        // 경매 마감 설정
        auction.setStatus(false);

        // 상위 입찰 설정
        auction.topBid(auction.getBuyNowPrice(),buyer.getNickname());

        // 구매자가 보유한 예치금에서 즉시구매 금액만큼 차감
        buyerMoney.useBalance(buyNowPrice);

        // 예치금 내역 생성
        BalanceHistory buyerHistory = BalanceHistory.builder()
                .memberId(buyer)
                .balance(buyNowPrice)
                .type(BalanceType.USE)
                .build();

        // 예치금 내역 저장
        balanceHistoryRepository.save(buyerHistory);

        BuyNowDto.Response response = BuyNowDto.Response.builder()
                .title(auction.getTitle())
                .buyNowPrice(buyNowPrice)
                .balance(buyerCanUseBalance - buyNowPrice)
                .build();

        return ResultDTO.of("성공적으로 즉시 구매되었습니다.", response);
    }
    @Transactional
    @Override
    public ResultDTO<BidCancelDto.Response> cancelBid(BidCancelDto.Request request) {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member buyer = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 입찰 취소하려는 경매 조회
        Auction cancelAuction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

        // 취소하려는 입찰 조회
        Bid cancelBid = bidRepository.findByAuctionIdAndBidder(cancelAuction, buyer)
                .orElseThrow(()-> new CustomException(NOT_FOUND_BID));


        // 비공개 입찰만 입찰 취소 가능
        if (cancelAuction.isAuctionType()) {
            throw new CustomException(NOT_PRIVATE_BID_CANCEL);

        }

        // 경매 마감인경우 입찰취소 불가능
        if (!cancelAuction.isStatus()) {
            throw new CustomException(TOO_OLD_AUCTION);

        }

        // 입찰 취소
        bidRepository.delete(cancelBid);

        BidCancelDto.Response response = BidCancelDto.Response.builder()
                .auctionId(cancelAuction.getAuctionId())
                .cancelPrice(cancelBid.getBidPrice())
                .build();

        return ResultDTO.of("성공적으로 입찰 취소 되었습니다.",response);
    }

    @Override
    public ResultDTO<BidSuccessListDto.Response> successBidAuctionList() {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 경매가 마감되어있으면서 인수 확인이 되어있지 않는 경매 물품 조회
        // 구매중인 경매물품 조회
        List<Auction> buyList = auctionRepository
                .findByStatusAndReceiveChkAndCurrentBidder(false, false,member.getNickname());

        // 판매중인 경매물품 조회
        List<Auction> sellList = auctionRepository
                .findByStatusAndReceiveChkAndMember(false, false,member);

        BidSuccessListDto.Response response = BidSuccessListDto.Response.builder()
                .buyList(buyList)
                .sellList(sellList)
                .build();

        return ResultDTO.of("성공적으로 거래중인 경매물품이 조회되었습니다.",response);
    }

    // 인계 확인
    @Transactional
    @Override
    public ResultDTO<HandOverDto.Response> sendChk(HandOverDto.Request request) {
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

        return ResultDTO.of("성공적으로 인계 확인이 되었습니다.",response);
    }

    // 인수 확인
    @Transactional
    @Override
    public ResultDTO<HandOverDto.Response> receiveChk(HandOverDto.Request request) {
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

        // 판매자 정보
        // 사용자 조회
        Member seller = memberRepository.findById(auction.getMember().getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 판매자가 보유한 예치금 조회
        Money sellerMoney = moneyRepository.findByMemberId(seller.getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));

        // 판매자가 보유한 예치금에서 낙찰된 금액만큼 증가
        sellerMoney.chargingBalance(auction.getCurrentBidPrice());

        // 예치금 내역 생성
        BalanceHistory sellerHistory = BalanceHistory.builder()
                .memberId(seller)
                .balance(auction.getCurrentBidPrice())
                .type(BalanceType.CHARGING)
                .createDate(LocalDateTime.now())
                .build();

        // 예치금 내역 저장
        balanceHistoryRepository.save(sellerHistory);

        // 인수 확인
        auction.receiveChking(true);

        HandOverDto.Response response = HandOverDto.Response.builder()
                .auctionId(auction.getAuctionId())
                .balance(auction.getCurrentBidPrice())
                .createDate(sellerHistory.getCreateDate())
                .build();

        return ResultDTO.of("성공적으로 인수 확인이 되었습니다.",response);
    }

    @Transactional
    @Override
    public ResultDTO<BidAuctionCancelDto.Response> cancelBidAuction(BidAuctionCancelDto.Request request) {
        // 구매 철회할 경매물품 조회
        Auction cancelBidAuction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

        // 이미 구매 철회되어있는 경매 물품인경우
        if (cancelBidAuction.isCancelChk()) {
            throw new CustomException(ALREADY_CANCEL_CHK);
        }

        // 이미 인수 확인이 되어있는 경우
        if (cancelBidAuction.isReceiveChk()) {
            throw new CustomException(ALREADY_RECEIVE_CHK);
        }

        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 구매자 조회
        Member buyer = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

        // 구매자 예치금 조회
        Money buyerMoney = moneyRepository.findByMemberId(buyer.getMemberId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_MONEY));

        // 구매자가 아닌 경우
        if (!buyer.getNickname().equals(cancelBidAuction.getCurrentBidder())) {
            throw new CustomException(NOT_AUCTION_BUYER);
        }

        // 취소 확인
        cancelBidAuction.cancelChking(true);

        // 구매자 예치금 원복 (10% 수수료 제외)
        long commissionBalance = (long) (cancelBidAuction.getCurrentBidPrice() * 0.9);
        buyerMoney.chargingBalance(commissionBalance);

        // 예치금 내역 생성
        BalanceHistory buyerHistory = BalanceHistory.builder()
                .memberId(buyer)
                .balance(commissionBalance)
                .type(BalanceType.CHARGING)
                .createDate(LocalDateTime.now())
                .build();

        // 예치금 내역 저장
        balanceHistoryRepository.save(buyerHistory);

        BidAuctionCancelDto.Response response = BidAuctionCancelDto.Response.builder()
                .auctionId(cancelBidAuction.getAuctionId())
                .balance(commissionBalance)
                .createDate(LocalDateTime.now())
                .build();
        return ResultDTO.of("성공적으로 구매 철회 되었습니다.",response);
    }


}
