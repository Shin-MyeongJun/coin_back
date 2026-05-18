package com.example.demo.api.config.security.ratelimit;

import java.time.Duration;

/**
 * Token bucket / fixed window rate limiter 추상화.
 */
public interface RateLimiterPort {

    /**
     * key 의 카운터를 1 증가시키고 limit 와 비교한다. window 만료 시각은 TTL 로 관리한다.
     *
     * @param key    bucket key (예: RedisKeys.rateLimitAccount(env, accountId, "m" + minute))
     * @param limit  허용 최대 요청 수
     * @param window window 길이 (1분/1일 등)
     * @return 허용/거부 + retryAfterMs
     */
    RateLimitDecision tryAcquire(String key, int limit, Duration window);
}
