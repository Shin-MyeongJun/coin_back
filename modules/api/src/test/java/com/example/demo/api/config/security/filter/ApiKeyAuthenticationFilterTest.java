package com.example.demo.api.config.security.filter;

import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.AuthenticatedApiKey;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ApiKeyAuthenticationFilterTest {

    private AuthenticateApiKeyUseCase auth;
    private ApiKeyAuthenticationFilter sut;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        auth = mock(AuthenticateApiKeyUseCase.class);
        objectMapper = new ObjectMapper();
        Clock clock = Clock.fixed(Instant.parse("2026-05-14T00:00:00Z"), ZoneOffset.UTC);
        sut = new ApiKeyAuthenticationFilter(auth, clock, objectMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Bearer scheme 이면 다음 필터로 위임한다")
    void bearerScheme_passThrough() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(auth);
    }

    @Test
    @DisplayName("유효 ApiKey 면 scope 와 policy 가 principal 에 보존된다")
    void validApiKey_preservesScopesAndPolicy() throws Exception {
        ApiKeyId apiKeyId = ApiKeyId.of(UUID.randomUUID());
        AccountId accountId = AccountId.of(UUID.randomUUID());
        Set<ApiKeyScope> scopes = Set.of(ApiKeyScope.READ_MARKET, ApiKeyScope.SSE_STREAM);
        RateLimitPolicy policy = new RateLimitPolicy(120, 100_000, 4);

        given(auth.authenticate(eq("ApiKey ys_abc.xyz"), any(), any(Instant.class)))
                .willReturn(Optional.of(new AuthenticatedApiKey(
                        apiKeyId, accountId, AccountTier.PRO, scopes, policy)));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "ApiKey ys_abc.xyz");
        req.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        AuthenticationPrincipal p = PrincipalSupport.current(req).orElseThrow();
        assertThat(p).isInstanceOf(ApiKeyPrincipal.class);
        ApiKeyPrincipal ap = (ApiKeyPrincipal) p;
        assertThat(ap.apiKeyId()).isEqualTo(apiKeyId);
        assertThat(ap.scopes()).containsExactlyInAnyOrderElementsOf(scopes);
        assertThat(ap.policy()).isEqualTo(policy);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isInstanceOf(AuthenticatedApiKey.class);
    }

    @Test
    @DisplayName("ApiKey 인증 실패 시 401 + API_KEY_INVALID 코드")
    void invalidApiKey_returns401() throws Exception {
        given(auth.authenticate(any(), any(), any())).willReturn(Optional.empty());

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "ApiKey ys_bad.bad");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        sut.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentAsString()).contains("\"code\":\"API_KEY_INVALID\"");
        verifyNoInteractions(chain);
    }
}
