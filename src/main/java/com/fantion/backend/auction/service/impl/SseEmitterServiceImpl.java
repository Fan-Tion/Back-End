package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.BidDto;
import com.fantion.backend.auction.repository.SseEmitterRepository;
import com.fantion.backend.auction.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {

    private final SseEmitterRepository sseEmitterRepository;

    @Value("${spring.sse.timeout}")
    private Long timeout;
    @Override
    public SseEmitter createEmitter(String emitterKey) {
        return sseEmitterRepository.save(emitterKey, new SseEmitter(timeout));
    }

    @Override
    public void deleteEmitter(String emitterKey) {
        sseEmitterRepository.deleteById(emitterKey);
    }

    @Override
    public void sendBidToClient(String emitterKey, BidDto.Response bid) {
        sseEmitterRepository.findById(emitterKey)
                .ifPresent(emitter -> send(bid, emitterKey, emitter));
    }

    @Override
    public void send(Object data, String emitterKey, SseEmitter sseEmitter) {
        try {
            log.info("send to client {}:[{}]", emitterKey, data);
            sseEmitter.send(SseEmitter.event()
                    .id(emitterKey)
                    .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            log.error("IOException | IllegalStateException is occurred. ", e);
            sseEmitterRepository.deleteById(emitterKey);
        }
    }
}
