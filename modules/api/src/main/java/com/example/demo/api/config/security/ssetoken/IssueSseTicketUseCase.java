package com.example.demo.api.config.security.ssetoken;

import com.example.demo.api.config.security.filter.AuthenticationPrincipal;

import java.time.Duration;
import java.time.Instant;

public interface IssueSseTicketUseCase {

    /**
     * 1회용 SSE ticket 발급.
     * @param principal 발급 요청자(이미 JWT/ApiKey로 인증된 상태여야 함)
     * @param ttl       Redis TTL (가이드 60s 고정)
     * @param now       기준 시각
     */
    Issued issue(AuthenticationPrincipal principal, Duration ttl, Instant now);

    record Issued(String ticket, Instant expiresAt) {}
}
