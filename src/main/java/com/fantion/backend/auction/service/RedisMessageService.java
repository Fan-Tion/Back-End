package com.fantion.backend.auction.service;

import com.fantion.backend.auction.dto.BidDto;

public interface RedisMessageService {

    // 채널 구독
    void subscribe(String channel);

    // 이벤트 발행
    void publish(String channel, BidDto.Response bidDto);

    // 구독 삭제
    void removeSubscribe(String channel);


}
