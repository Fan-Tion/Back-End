package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.configuration.RedisPublisher;
import com.fantion.backend.configuration.RedisSubscriber;
import com.fantion.backend.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {
  private final AuctionRepository auctionRepository;
  private final BidRepository bidRepository;
  private final RedisMessageListenerContainer redisMessageListenerContainer;
  private final RedisSubscriber redisSubscriber;
  private final RedisPublisher redisPublisher;
  private Map<String, ChannelTopic> channels = new HashMap<>();

  @Override
  public Response createAuction(Request request) {
    Auction auction = toAuction(request);

    auctionRepository.save(auction);

    return toResponse(auction);
  }

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

    // 입찰가 갱신
    // 1. 입찰 하려는 경매 물품의 식별자로 만들어진 채널 가져오기
    String channelName = String.valueOf(auction.getAuctionId());
    ChannelTopic channel = channels.get(channelName);

    // 2. 가져온 채널에서 구독중인 회원에게 메세지 전달
    BidDto.Response response = BidDto.Response(bidRepository.save(bid));
    redisPublisher.publish(channel, response);

    return response;
  }

  // 경매 상세보기
  @Override
  public AuctionDto.Response findAuction(Long auctionId) {
    // 상세보기할 경매 조회
    Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(()-> new RuntimeException());

    // 경매 물품 식별자로 만들어진 채널 생성
    String channelName = String.valueOf(auction.getAuctionId());
    ChannelTopic channel = new ChannelTopic(channelName);

    // 생성한 채널 구독
    redisMessageListenerContainer.addMessageListener(redisSubscriber, channel);
    channels.put(channelName, channel);

    return toResponse(auction);

  }


  private Auction toAuction(Request request) {
    return Auction.builder()
        .member(new Member())
        .title(request.getTitle())
        .auctionType(request.isAuctionType())
        .auctionImage(null)
        .description(request.getDescription())
        .currentBidPrice(request.getCurrentBidPrice())
        .currentBidder(null)
        .buyNowPrice(request.getBuyNowPrice())
        .favoriteCnt(0L)
        .createDate(LocalDateTime.now())
        .endDate(request.getEndDate())
        //.status(true)
        .build();
  }

  private Response toResponse(Auction auction) {
    // 회원 닉네임 구현 필요

    return Response.builder()
        .title(auction.getTitle())
        .auctionUserNickname(null)
        .auctionType(auction.isAuctionType())
        .description(auction.getDescription())
        .currentBidPrice(auction.getCurrentBidPrice())
        .currentBidder(auction.getCurrentBidder())
        .buyNowPrice(auction.getBuyNowPrice())
        .favoritePrice(auction.getFavoriteCnt())
        .createDate(auction.getCreateDate())
        .endDate(auction.getEndDate())
        //.status(auction.isStatus())
        .build();
  }
}
