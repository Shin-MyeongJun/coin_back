package com.example.demo.api.config.security;

import com.example.demo.api.config.security.ratelimit.RateLimitDecision;
import com.example.demo.api.config.security.ratelimit.RateLimiterPort;
import com.example.demo.api.config.security.ssetoken.ConsumeSseTicketPort;
import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.LoadRateLimitPolicyQuery;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @WebMvcTest 슬라이스에서 SecurityConfig 가 요구하는 외부 모듈 의존성을
 * no-op mock 으로 채워주는 공용 테스트 보조 설정.
 *
 * <p>controller/global-exception/stream 테스트는 인증/레이트리밋 동작 자체를 검증하지 않으므로
 * 모든 요청을 통과시키는 mock 으로 충분하다.
 */
@TestConfiguration
public class SecurityTestSupport {

    @Bean
    @Primary
    public ConsumeSseTicketPort consumeSseTicketPortMock() {
        return mock(ConsumeSseTicketPort.class);
    }

    @Bean
    @Primary
    public RateLimiterPort rateLimiterPortMock() {
        RateLimiterPort port = mock(RateLimiterPort.class);
        given(port.tryAcquire(any(), org.mockito.ArgumentMatchers.anyInt(), any()))
                .willReturn(RateLimitDecision.allow());
        return port;
    }

    @Bean
    @Primary
    public VerifyAccessTokenUseCase verifyAccessTokenUseCaseMock() {
        return mock(VerifyAccessTokenUseCase.class);
    }

    @Bean
    @Primary
    public AuthenticateApiKeyUseCase authenticateApiKeyUseCaseMock() {
        return mock(AuthenticateApiKeyUseCase.class);
    }

    @Bean
    @Primary
    public LoadRateLimitPolicyQuery loadRateLimitPolicyQueryMock() {
        LoadRateLimitPolicyQuery q = mock(LoadRateLimitPolicyQuery.class);
        RateLimitPolicy policy = new RateLimitPolicy(120, 100_000, 4);
        given(q.forAccount(any())).willReturn(policy);
        given(q.forApiKey(any())).willReturn(policy);
        return q;
    }

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-05-14T00:00:00Z"), ZoneOffset.UTC);
    }
}
