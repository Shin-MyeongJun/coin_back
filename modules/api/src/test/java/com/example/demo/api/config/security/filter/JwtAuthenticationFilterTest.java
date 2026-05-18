package com.example.demo.api.config.security.filter;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class JwtAuthenticationFilterTest {

    private VerifyAccessTokenUseCase verify;
    private JwtAuthenticationFilter sut;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        verify = mock(VerifyAccessTokenUseCase.class);
        objectMapper = new ObjectMapper();
        Clock clock = Clock.fixed(Instant.parse("2026-05-14T00:00:00Z"), ZoneOffset.UTC);
        sut = new JwtAuthenticationFilter(verify, clock, objectMapper);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 그대로 통과한다")
    void noHeader_passThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(verify);
        assertThat(PrincipalSupport.current(req)).isEmpty();
    }

    @Test
    @DisplayName("Bearer 가 아니면 그대로 통과한다")
    void nonBearerScheme_passThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "ApiKey ys_abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(verify);
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 JwtPrincipal 을 attribute 에 주입한다")
    void validBearer_injectsPrincipal() throws Exception {
        AccountId id = AccountId.of(UUID.randomUUID());
        AuthenticatedAccount account = new AuthenticatedAccount(
                id, new Email("user@example.com"), AccountTier.PRO);
        given(verify.verify(eq("Bearer abc"), any(Instant.class)))
                .willReturn(Optional.of(account));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        AuthenticationPrincipal p = PrincipalSupport.current(req).orElseThrow();
        assertThat(p).isInstanceOf(JwtPrincipal.class);
        JwtPrincipal jp = (JwtPrincipal) p;
        assertThat(jp.accountId()).isEqualTo(id);
        assertThat(jp.tier()).isEqualTo(AccountTier.PRO);
    }

    @Test
    @DisplayName("만료/위조 토큰이면 401 응답을 직접 작성하고 체인을 종료한다")
    void invalidBearer_writes401() throws Exception {
        given(verify.verify(any(), any())).willReturn(Optional.empty());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer expired");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentType()).startsWith("application/problem+json");
        assertThat(res.getContentAsString()).contains("\"code\":\"JWT_INVALID\"");
        verifyNoInteractions(chain);
    }

    @Test
    @DisplayName("이미 principal 이 주입되어 있으면 검증을 건너뛴다 (SSE 티켓 이후)")
    void principalAlreadyPresent_skipsVerification() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        PrincipalSupport.store(req, new AnonymousPrincipal("1.2.3.4"));
        req.addHeader("Authorization", "Bearer abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(verify);
    }
}
