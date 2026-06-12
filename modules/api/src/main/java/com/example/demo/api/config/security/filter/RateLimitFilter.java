package com.example.demo.api.config.security.filter;

import com.example.demo.api.config.security.error.ProblemDetails;
import com.example.demo.api.config.security.ratelimit.RateLimitDecision;
import com.example.demo.api.config.security.ratelimit.RateLimiterPort;
import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.user.application.port.in.LoadRateLimitPolicyQuery;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

/**
 * Token-bucket rate limit 적용.
 *
 * <p>Principal 종류별 키 산정:
 * <ul>
 *   <li>{@link ApiKeyPrincipal}  → {@link RedisKeys#rateLimitApiKey} (분/일 bucket)</li>
 *   <li>{@link JwtPrincipal}     → {@link RedisKeys#rateLimitAccount} (분 bucket)
 *                                  + {@link LoadRateLimitPolicyQuery#forAccount}</li>
 *   <li>{@link AnonymousPrincipal} 또는 미주입 → IP 기반 fallback
 *                                  ({@link RedisKeys#rateLimitIp}, 기본 rpm=30)</li>
 * </ul>
 *
 * <p>거부 시 429 + Retry-After 헤더 + RFC 7807.
 */
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterPort rateLimiterPort;
    private final LoadRateLimitPolicyQuery loadRateLimitPolicyQuery;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Value("${app.env:local}")
    private String env;

    @Value("${ys.security.rate-limit.anonymous-rpm:30}")
    private int anonymousRpm;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        long nowMs = clock.millis();
        long minuteEpoch = nowMs / 60_000L;
        long dayEpoch = nowMs / 86_400_000L;

        AuthenticationPrincipal principal = PrincipalSupport.current(request)
                .orElseGet(() -> new AnonymousPrincipal(PrincipalSupport.clientIp(request)));

        RateLimitDecision decision = switch (principal) {
            case ApiKeyPrincipal a -> evaluateApiKey(a, minuteEpoch, dayEpoch);
            case JwtPrincipal j -> evaluateAccount(j, minuteEpoch);
            case AnonymousPrincipal anon -> evaluateAnonymous(anon, minuteEpoch);
        };

        if (!decision.allowed()) {
            ProblemDetails.write(response, objectMapper, HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded",
                    "Too many requests; retry later",
                    "RATE_LIMIT_EXCEEDED",
                    decision.retryAfterSeconds());
            return;
        }
        chain.doFilter(request, response);
    }

    private RateLimitDecision evaluateApiKey(ApiKeyPrincipal principal, long minute, long day) {
        RateLimitPolicy policy = principal.policy();
        String apiKeyId = principal.apiKeyId().asString();

        RateLimitDecision m = rateLimiterPort.tryAcquire(
                RedisKeys.rateLimitApiKey(env, apiKeyId, "m" + minute),
                policy.rpm(), Duration.ofMinutes(1));
        if (!m.allowed()) return m;

        return rateLimiterPort.tryAcquire(
                RedisKeys.rateLimitApiKey(env, apiKeyId, "d" + day),
                policy.rpd(), Duration.ofDays(1));
    }

    private RateLimitDecision evaluateAccount(JwtPrincipal principal, long minute) {
        RateLimitPolicy policy = loadRateLimitPolicyQuery.forAccount(principal.accountId());
        String accountId = principal.accountId().asString();
        return rateLimiterPort.tryAcquire(
                RedisKeys.rateLimitAccount(env, accountId, "m" + minute),
                policy.rpm(), Duration.ofMinutes(1));
    }

    private RateLimitDecision evaluateAnonymous(AnonymousPrincipal anon, long minute) {
        return rateLimiterPort.tryAcquire(
                RedisKeys.rateLimitIp(env, anon.clientIp(), "m" + minute),
                anonymousRpm, Duration.ofMinutes(1));
    }
}
