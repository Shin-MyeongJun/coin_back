package com.example.demo.api.config.security.ssetoken;

import com.example.demo.api.config.security.filter.AnonymousPrincipal;
import com.example.demo.api.config.security.filter.ApiKeyPrincipal;
import com.example.demo.api.config.security.filter.JwtPrincipal;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IssueSseTicketServiceTest {

    @Test
    @DisplayName("JwtPrincipal → JWT payload, TTL 만큼 만료 시각 반환")
    void issueForJwt() {
        ConsumeSseTicketPort port = mock(ConsumeSseTicketPort.class);
        given(port.store(anyString(), any(SseTicketPayload.class), any(Duration.class)))
                .willReturn(true);

        IssueSseTicketService sut = new IssueSseTicketService(port);

        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        IssueSseTicketUseCase.Issued issued = sut.issue(
                new JwtPrincipal(AccountId.of(UUID.randomUUID()), AccountTier.PRO),
                Duration.ofSeconds(60), now);

        assertThat(issued.ticket()).isNotBlank();
        assertThat(issued.expiresAt()).isEqualTo(now.plusSeconds(60));
    }

    @Test
    @DisplayName("ApiKeyPrincipal → API_KEY payload, scope/policy 포함")
    void issueForApiKey() {
        ConsumeSseTicketPort port = mock(ConsumeSseTicketPort.class);
        given(port.store(anyString(), any(SseTicketPayload.class), any(Duration.class)))
                .willReturn(true);

        IssueSseTicketService sut = new IssueSseTicketService(port);

        ApiKeyPrincipal principal = new ApiKeyPrincipal(
                ApiKeyId.of(UUID.randomUUID()),
                AccountId.of(UUID.randomUUID()),
                AccountTier.PRO,
                Set.of(ApiKeyScope.SSE_STREAM),
                new RateLimitPolicy(120, 100_000, 4));

        IssueSseTicketUseCase.Issued issued = sut.issue(
                principal, Duration.ofSeconds(60), Instant.now());

        assertThat(issued.ticket()).isNotBlank();
        verify(port).store(eq(issued.ticket()),
                any(SseTicketPayload.class),
                eq(Duration.ofSeconds(60)));
    }

    @Test
    @DisplayName("Anonymous principal 은 ticket 발급 거부")
    void anonymousRejected() {
        ConsumeSseTicketPort port = mock(ConsumeSseTicketPort.class);
        IssueSseTicketService sut = new IssueSseTicketService(port);

        assertThatThrownBy(() -> sut.issue(new AnonymousPrincipal("1.2.3.4"),
                Duration.ofSeconds(60), Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Store 충돌 시 최대 3회 재시도 후 실패")
    void storeRetryThenFail() {
        ConsumeSseTicketPort port = mock(ConsumeSseTicketPort.class);
        given(port.store(anyString(), any(), any())).willReturn(false);

        IssueSseTicketService sut = new IssueSseTicketService(port);

        assertThatThrownBy(() -> sut.issue(
                new JwtPrincipal(AccountId.of(UUID.randomUUID()), AccountTier.FREE),
                Duration.ofSeconds(60), Instant.now()))
                .isInstanceOf(IllegalStateException.class);
        verify(port, times(3)).store(anyString(), any(), any());
    }
}
