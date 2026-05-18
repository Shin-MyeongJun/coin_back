package com.example.demo.api.config.security.filter;

import com.example.demo.api.config.security.ssetoken.ConsumeSseTicketPort;
import com.example.demo.api.config.security.ssetoken.SseTicketPayload;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SseTicketFilterTest {

    private ConsumeSseTicketPort consume;
    private SseTicketFilter sut;

    @BeforeEach
    void setUp() {
        consume = mock(ConsumeSseTicketPort.class);
        sut = new SseTicketFilter(consume, new ObjectMapper());
    }

    @Test
    @DisplayName("/api/v1/stream 외 경로는 그대로 통과")
    void nonStreamPath_passThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/market/tick/latest");
        req.setRequestURI("/api/v1/market/tick/latest");
        req.setParameter("t", "abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(consume);
    }

    @Test
    @DisplayName("stream 경로지만 t 쿼리 없으면 그대로 통과 (다른 인증 방식 위임)")
    void streamPath_noTicket_passThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/stream/ticks");
        req.setRequestURI("/api/v1/stream/ticks");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(consume);
    }

    @Test
    @DisplayName("유효한 ticket: ApiKeyPrincipal 을 attribute 에 주입")
    void validTicket_apiKey_injectsPrincipal() throws Exception {
        ApiKeyId apiKeyId = ApiKeyId.of(UUID.randomUUID());
        AccountId accountId = AccountId.of(UUID.randomUUID());
        SseTicketPayload payload = SseTicketPayload.apiKey(
                apiKeyId, accountId, AccountTier.PRO,
                Set.of(ApiKeyScope.SSE_STREAM),
                new RateLimitPolicy(120, 100_000, 4));
        given(consume.consume("good")).willReturn(Optional.of(payload));

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/stream/ticks");
        req.setRequestURI("/api/v1/stream/ticks");
        req.setParameter("t", "good");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        AuthenticationPrincipal p = PrincipalSupport.current(req).orElseThrow();
        assertThat(p).isInstanceOf(ApiKeyPrincipal.class);
        assertThat(((ApiKeyPrincipal) p).apiKeyId()).isEqualTo(apiKeyId);
    }

    @Test
    @DisplayName("이미 소비된/존재하지 않는 ticket 은 401 + SSE_TICKET_INVALID")
    void invalidTicket_returns401() throws Exception {
        given(consume.consume("used")).willReturn(Optional.empty());

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/stream/ticks");
        req.setRequestURI("/api/v1/stream/ticks");
        req.setParameter("t", "used");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentAsString()).contains("\"code\":\"SSE_TICKET_INVALID\"");
        verifyNoInteractions(chain);
    }
}
