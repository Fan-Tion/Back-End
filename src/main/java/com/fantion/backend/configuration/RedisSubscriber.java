package com.fantion.backend.configuration;

import com.fantion.backend.auction.dto.BidDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("메세지 : {}",message);
            // String -> BidDto 변환
            BidDto.Response bid = objectMapper.readValue( message.getBody(), BidDto.Response.class);

            // redis에 저장
            redisTemplate.opsForValue().set(String.valueOf(bid.getAuctionId()),String.valueOf(bid.getBidPrice()));

            // 메세지 전달
            messagingTemplate.convertAndSend("/sub/auction/view/"+bid.getAuctionId(), bid);

        } catch (Exception e) {
            log.error(e.getMessage());
        }



    }
}
