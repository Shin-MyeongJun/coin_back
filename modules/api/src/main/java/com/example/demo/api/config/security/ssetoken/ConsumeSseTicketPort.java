package com.example.demo.api.config.security.ssetoken;

import java.time.Duration;
import java.util.Optional;

/**
 * SSE ticket Redis 어댑터 추상화. 발급/1회 소비를 atomic 하게 보장한다.
 */
public interface ConsumeSseTicketPort {

    /**
     * Redis SET NX. 충돌 시 false.
     */
    boolean store(String token, SseTicketPayload payload, Duration ttl);

    /**
     * Redis GETDEL. 1회 소비 후 즉시 삭제.
     */
    Optional<SseTicketPayload> consume(String token);
}
