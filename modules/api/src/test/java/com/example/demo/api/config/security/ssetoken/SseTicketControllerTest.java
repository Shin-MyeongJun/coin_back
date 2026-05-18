package com.example.demo.api.config.security.ssetoken;

import com.example.demo.api.config.security.filter.AnonymousPrincipal;
import com.example.demo.api.config.security.filter.AuthenticationPrincipal;
import com.example.demo.api.config.security.filter.JwtPrincipal;
import com.example.demo.api.config.security.filter.PrincipalSupport;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class SseTicketControllerTest {

    private IssueSseTicketUseCase useCase;
    private SseTicketController sut;
    private static final Instant NOW = Instant.parse("2026-05-14T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = mock(IssueSseTicketUseCase.class);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        sut = new SseTicketController(useCase, clock);
    }

    @Test
    @DisplayName("인증된 principal 에 대해 ticket + expiresAt(epochMs) 반환")
    void issuesForAuthenticated() {
        AuthenticationPrincipal principal = new JwtPrincipal(
                AccountId.of(UUID.randomUUID()), AccountTier.PRO);
        given(useCase.issue(any(), any(Duration.class), any(Instant.class)))
                .willReturn(new IssueSseTicketUseCase.Issued("tok", NOW.plusSeconds(60)));

        MockHttpServletRequest req = new MockHttpServletRequest();
        PrincipalSupport.store(req, principal);

        ResponseEntity<SseTicketController.SseTicketResponse> response = sut.issue(req);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().ticket()).isEqualTo("tok");
        assertThat(response.getBody().expiresAt()).isEqualTo(NOW.plusSeconds(60).toEpochMilli());
    }

    @Test
    @DisplayName("Anonymous principal 은 401")
    void anonymousRejected() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        PrincipalSupport.store(req, new AnonymousPrincipal("1.2.3.4"));

        assertThatThrownBy(() -> sut.issue(req))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("Principal 미주입 → 401")
    void noPrincipal_returns401() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        assertThatThrownBy(() -> sut.issue(req))
                .isInstanceOf(ResponseStatusException.class);
    }
}
