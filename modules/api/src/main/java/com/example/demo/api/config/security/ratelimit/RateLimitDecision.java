package com.example.demo.api.config.security.ratelimit;

/**
 * Rate limit 평가 결과.
 *
 * @param allowed       허용 여부
 * @param retryAfterMs  거부 시 다음 시도 가능 시각까지 밀리초(허용 시 0). Retry-After 헤더 변환에 사용.
 */
public record RateLimitDecision(boolean allowed, long retryAfterMs) {

    public static RateLimitDecision allow() {
        return new RateLimitDecision(true, 0L);
    }

    public static RateLimitDecision deny(long retryAfterMs) {
        return new RateLimitDecision(false, Math.max(0L, retryAfterMs));
    }

    public long retryAfterSeconds() {
        return (retryAfterMs + 999L) / 1000L;
    }
}
