package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.member.entity.BalanceHistory;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.Money;
import com.fantion.backend.member.repository.BalanceHistoryRepository;
import com.fantion.backend.member.repository.MoneyRepository;
import com.fantion.backend.type.BalanceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BidServiceImplTest {

    @Mock
    private MoneyRepository moneyRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @InjectMocks
    private BidServiceImpl bidService;


    @Test
    @DisplayName("보유한 예치금보다 입찰가 합산이 더 적을경우 입찰 성공")
    void balanceCheck() {
        // 회원
        Member ysg = Member.builder()
                .memberId(1L)
                .nickname("ysg")
                .build();

        // 예치금
        Money ysgMoney = Money.builder()
                .member(ysg)
                .balance(17000L)
                .build();

        // 경매 물품
        Auction album = Auction.builder()
                .auctionId(1L)
                .title("album")
                .currentBidPrice(5000L)
                .currentBidder(ysg.getNickname())
                .build();

        Auction photoCard = Auction.builder()
                .auctionId(2L)
                .title("photoCard")
                .currentBidPrice(6000L)
                .currentBidder(ysg.getNickname())
                .build();

        List<Auction> auctionList = new ArrayList<>();
        auctionList.add(album);
        auctionList.add(photoCard);

        //given
        // 예치금
        // 총 1만 7천원 보유
        given(moneyRepository.findByMemberId(ysg.getMemberId()))
                .willReturn(Optional.ofNullable(ysgMoney));

        // 입찰내역
        // 앨범에 5000원, 포토카드에 6000원으로 상위 입찰되어있는 상태 (총 11000원이 입찰에 사용중)
        given(auctionRepository.findByCurrentBidderAndStatus(ysg.getNickname(),true))
                .willReturn(auctionList);

        //when
        Long canUseBalance = bidService.balanceCheck(ysg);

        //then
        // 보유한 예치금은 17000원이고 입찰에 11000원이 사용중이므로 사용 가능한 예치금은 6000원임을 예상
        assertEquals(canUseBalance,6000L);

    }

    @Test
    @DisplayName("낙찰시 보유한 예치금에서 입찰가만큼 차감")
    void finishBid() {
        //given
        // 경매 물품
        // 종료된 경매 물품
        Auction album = Auction.builder()
                .auctionId(1L)
                .title("album")
                .endDate(LocalDateTime.parse("2024-07-07T10:00:00"))
                .build();

        Auction photoCard = Auction.builder()
                .auctionId(2L)
                .title("photoCard")
                .endDate(LocalDateTime.parse("2024-07-07T11:00:00"))
                .build();

        // 진행중인 경매 물품
        Auction figure = Auction.builder()
                .auctionId(3L)
                .title("figure")
                .endDate(LocalDateTime.parse("2024-07-17T10:00:00"))
                .build();

        // 회원
        Member ysg = Member.builder()
                .memberId(1L)
                .nickname("ysg")
                .build();

        Member tester = Member.builder()
                .memberId(2L)
                .nickname("tester")
                .build();

        // 예치금
        Money ysgMoney = Money.builder()
                .member(ysg)
                .balance(10000L)
                .build();

        Money testerMoney = Money.builder()
                .member(tester)
                .balance(20000L)
                .build();

        // 입찰
        Bid ysgBid = Bid.builder()
                .bidId(1L)
                .auctionId(album)
                .bidPrice(5000L)
                .bidder(ysg)
                .build();

        Bid testerBid = Bid.builder()
                .bidId(2L)
                .auctionId(photoCard)
                .bidPrice(7000L)
                .bidder(tester)
                .build();

        List<Auction> auctionList = new ArrayList<>();
        auctionList.add(album);
        auctionList.add(photoCard);
        auctionList.add(figure);

        BalanceHistory ysgHistory = BalanceHistory.builder()
                .memberId(ysg)
                .balance(ysgBid.getBidPrice())
                .type(BalanceType.USE)
                .build();

        BalanceHistory testerHistory = BalanceHistory.builder()
                .memberId(tester)
                .balance(testerBid.getBidPrice())
                .type(BalanceType.USE)
                .build();

        // 경매 물품 리스트 모킹
        given(auctionRepository.findAll())
                .willReturn(auctionList);

        // 종료일이 지난 경매물품 모킹
        given(bidRepository.findByAuctionIdOrderByBidPriceDesc(album))
                .willReturn(Optional.ofNullable(ysgBid));
        given(bidRepository.findByAuctionIdOrderByBidPriceDesc(photoCard))
                .willReturn(Optional.ofNullable(testerBid));

        // 입찰자의 예치금 모킹
        given(moneyRepository.findByMemberId(ysgBid.getBidder().getMemberId()))
                .willReturn(Optional.ofNullable(ysgMoney));
        given(moneyRepository.findByMemberId(testerBid.getBidder().getMemberId()))
                .willReturn(Optional.ofNullable(testerMoney));

        // 예치금 저장 모킹
        given(balanceHistoryRepository.save(any()))
                .willReturn(ysgHistory);
        //when
        bidService.finishBid();
        //then
        // 예치금 1만원을 보유한 ysg는 앨범 5천원에 입찰하여 낙찰받았으므로 남아있는 예치금은 5천원
        assertEquals(ysgMoney.getBalance(),5000L);

        // 예치금 2만원을 보유한 tester는 포토카드 7천원에 입찰하여 낙찰받았으므로 남아있는 예치금은 1만3천원
        assertEquals(testerMoney.getBalance(),13000L);

    }


}