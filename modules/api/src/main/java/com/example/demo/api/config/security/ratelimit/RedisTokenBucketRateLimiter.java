package com.example.demo.api.config.security.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Redis Lua 기반 fixed-window 카운터.
 *
 * <p>Lua 스크립트가 INCR + PEXPIRE + PTTL 을 단일 round-trip 으로 묶어 atomicity 를 보장한다.
 * 반환값은 {@code {count, pttl}} 두 정수.
 *
 * <ul>
 *   <li>count &lt;= limit → allow</li>
 *   <li>count &gt; limit  → deny, retryAfterMs = pttl (남은 window)</li>
 * </ul>
 *
 * <p>PTTL 이 -1(영구) 인 경우는 EXPIRE 실패한 비정상 상황이라
 *    window 전체를 retryAfter 로 반환해 안전 측에 둔다.
 */
@Component
@RequiredArgsConstructor
public class RedisTokenBucketRateLimiter implements RateLimiterPort {

    private static final byte[] LUA = ("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
              redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            local pttl = redis.call('PTTL', KEYS[1])
            return {current, pttl}
            """).getBytes(StandardCharsets.UTF_8);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public RateLimitDecision tryAcquire(String key, int limit, Duration window) {
        if (limit <= 0) {
            return RateLimitDecision.deny(window.toMillis());
        }
        long windowMs = Math.max(1L, window.toMillis());

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] windowBytes = Long.toString(windowMs).getBytes(StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute((RedisCallback<List<Long>>) connection ->
                (List<Long>) connection.scriptingCommands().eval(
                        LUA, ReturnType.MULTI, 1, keyBytes, windowBytes));

        if (result == null || result.size() < 2) {
            return RateLimitDecision.allow();
        }
        long count = result.get(0);
        long pttl = result.get(1);
        if (count <= limit) {
            return RateLimitDecision.allow();
        }
        long retryAfterMs = (pttl < 0) ? windowMs : pttl;
        return RateLimitDecision.deny(retryAfterMs);
    }
}
