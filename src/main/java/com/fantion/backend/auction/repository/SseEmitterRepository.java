package com.fantion.backend.auction.repository;

import java.util.Optional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterRepository {

    SseEmitter save(String eventId, SseEmitter sseEmitter);
    Optional<SseEmitter> findById(String memberId);
    void deleteById(String eventId);
}
