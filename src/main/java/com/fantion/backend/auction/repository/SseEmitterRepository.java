package com.fantion.backend.auction.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

public interface SseEmitterRepository {

    SseEmitter save(String eventId, SseEmitter sseEmitter);
    Optional<SseEmitter> findById(String memberId);
    void deleteById(String eventId);
}
