package com.example.demo.api.config.security.filter;

import com.example.demo.api.config.security.ratelimit.RateLimitDecision;
import com.example.demo.api.config.security.ratelimit.RateLimiterPort;
import com.example.demo.user.application.port.in.LoadRateLimitPolicyQuery;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RateLimitFilterTest {

    private RateLimiterPort rateLimiter;
    private LoadRateLimitPolicyQuery loadPolicy;
    private RateLimitFilter sut;

    @BeforeEach
    void setUp() {
        rateLimiter = mock(RateLimiterPort.class);
        loadPolicy = mock(LoadRateLimitPolicyQuery.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-14T00:00:00Z"), ZoneOffset.UTC);
        sut = new RateLimitFilter(rateLimiter, loadPolicy, clock, new ObjectMapper());
        ReflectionTestUtils.setField(sut, "env", "test");
        ReflectionTestUtils.setField(sut, "anonymousRpm", 30);
    }

    @Test
    @DisplayName("ApiKeyPrincipal: 분/일 두 버킷 모두 acquire 한다")
    void apiKey_minuteAndDay() throws Exception {
        ApiKeyPrincipal principal = new ApiKeyPrincipal(
                ApiKeyId.of(UUID.randomUUID()),
                AccountId.of(UUID.randomUUID()),
                AccountTier.PRO,
                Set.of(ApiKeyScope.READ_MARKET),
                new RateLimitPolicy(120, 100_000, 4));

        given(rateLimiter.tryAcquire(any(), anyInt(), any())).willReturn(RateLimitDecision.allow());

        MockHttpServletRequest req = new MockHttpServletRequest();
        PrincipalSupport.store(req, principal);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(rateLimiter).tryAcquire(contains(":ratelimit:apikey:"), eqInt(120), eqDuration(Duration.ofMinutes(1)));
        verify(rateLimiter).tryAcquire(contains(":ratelimit:apikey:"), eqInt(100_000), eqDuration(Duration.ofDays(1)));
    }

    @Test
    @DisplayName("RateLimit 초과 시 429 + Retry-After 헤더")
    void exceeded_returns429() throws Exception {
        ApiKeyPrincipal principal = new ApiKeyPrincipal(
                ApiKeyId.of(UUID.randomUUID()),
                AccountId.of(UUID.randomUUID()),
                AccountTier.FREE,
                Set.of(ApiKeyScope.READ_MARKET),
                new RateLimitPolicy(60, 10_000, 1));

        given(rateLimiter.tryAcquire(any(), anyInt(), any()))
                .willReturn(RateLimitDecision.deny(45_000L));

        MockHttpServletRequest req = new MockHttpServletRequest();
        PrincipalSupport.store(req, principal);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isEqualTo("45");
        assertThat(res.getContentAsString()).contains("\"code\":\"RATE_LIMIT_EXCEEDED\"");
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("Principal 미주입 시 IP 기반 익명 fallback 사용 (rpm=30)")
    void anonymous_ipFallback() throws Exception {
        given(rateLimiter.tryAcquire(any(), anyInt(), any())).willReturn(RateLimitDecision.allow());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("203.0.113.5");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(rateLimiter).tryAcquire(contains(":ratelimit:ip:203.0.113.5:"), eqInt(30), eqDuration(Duration.ofMinutes(1)));
    }

    @Test
    @DisplayName("JwtPrincipal: account 정책을 LoadRateLimitPolicyQuery 에서 조회한다")
    void jwt_loadsAccountPolicy() throws Exception {
        AccountId accountId = AccountId.of(UUID.randomUUID());
        given(loadPolicy.forAccount(accountId)).willReturn(new RateLimitPolicy(90, 50_000, 2));
        given(rateLimiter.tryAcquire(any(), anyInt(), any())).willReturn(RateLimitDecision.allow());

        MockHttpServletRequest req = new MockHttpServletRequest();
        PrincipalSupport.store(req, new JwtPrincipal(accountId, AccountTier.FREE));
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(loadPolicy).forAccount(accountId);
        verify(rateLimiter).tryAcquire(contains(":ratelimit:account:"), eqInt(90), eqDuration(Duration.ofMinutes(1)));
    }

    private static int eqInt(int v) { return org.mockito.ArgumentMatchers.eq(v); }
    private static Duration eqDuration(Duration v) { return org.mockito.ArgumentMatchers.eq(v); }
}
