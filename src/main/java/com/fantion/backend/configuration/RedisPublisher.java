package com.fantion.backend.configuration;

import com.fantion.backend.auction.dto.BidDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 입찰 정보 발행
    public void publish(ChannelTopic topic, String bidPrice) {
        redisTemplate.convertAndSend(topic.getTopic(), bidPrice);
    }
    public void publish(ChannelTopic topic, BidDto.Response bid)  {
        try {
            // BidDto -> String 으로 변환
            String message = objectMapper.writeValueAsString(bid);

            // 채널의 구독자에게 메세지 전달
            redisTemplate.convertAndSend(topic.getTopic(), message);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

}
