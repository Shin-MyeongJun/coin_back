package com.example.demo.alert.infrastructure.sse;

import com.example.demo.alert.infrastructure.web.dto.AlertFiringResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AlertSseRegistry {
    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;

    private final ConcurrentHashMap<String, Set<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public SseEmitter register(String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emittersByUserId.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(emitter);
        emitter.onCompletion(() -> unregister(userId, emitter));
        emitter.onTimeout(() -> unregister(userId, emitter));
        emitter.onError(error -> unregister(userId, emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("{}"));
        } catch (Exception e) {
            emitter.completeWithError(e);
            unregister(userId, emitter);
        }
        return emitter;
    }

    public void broadcast(String userId, AlertFiringResponse response) {
        Set<SseEmitter> emitters = emittersByUserId.getOrDefault(userId, Set.of());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alert-firing")
                        .data(response));
            } catch (Exception e) {
                emitter.completeWithError(e);
                unregister(userId, emitter);
            }
        }
    }

    private void unregister(String userId, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersByUserId.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUserId.remove(userId, emitters);
        }
    }
}
