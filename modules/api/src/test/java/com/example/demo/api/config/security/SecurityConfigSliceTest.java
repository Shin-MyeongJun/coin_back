package com.example.demo.api.config.security;

import com.example.demo.api.config.CorsConfig;
import com.example.demo.api.config.SecurityConfig;
import com.example.demo.api.config.security.SecurityConfigSliceTest.StubAuthMeController;
import com.example.demo.api.config.security.SecurityConfigSliceTest.StubProtectedController;
import com.example.demo.api.config.security.SecurityConfigSliceTest.StubPublicController;
import com.example.demo.api.config.security.SecurityConfigSliceTest.StubStreamController;
import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.AuthenticatedApiKey;
import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfig 의 인가 규칙 자체를 검증하는 slice 테스트.
 *
 * <p>실제 controller 가 존재하지 않는 경로(예: /api/v1/auth/me, /api/v1/alert/rules)는
 * 본 테스트 내 stub controller 로 대체하여 SecurityFilterChain 의 인가 동작을 확인한다.
 * - permitAll 경로는 controller 본문이 실행되어 200/지정 본문이 반환된다.
 * - authenticated 경로는 토큰이 없을 때 401 ProblemDetail 응답으로 끊긴다.
 * - SSE 경로는 ?access_token= 쿼리 fallback 으로 토큰 검증을 통과시킨다.
 */
@WebMvcTest(controllers = {
        StubPublicController.class,
        StubProtectedController.class,
        StubAuthMeController.class,
        StubStreamController.class
})
@Import({
        CorsConfig.class,
        SecurityConfig.class,
        SecurityTestSupport.class,
        StubPublicController.class,
        StubProtectedController.class,
        StubAuthMeController.class,
        StubStreamController.class
})
class SecurityConfigSliceTest {

    @Autowired MockMvc mvc;
    @Autowired VerifyAccessTokenUseCase verifyAccessTokenUseCase;
    @Autowired AuthenticateApiKeyUseCase authenticateApiKeyUseCase;

    @Test
    @DisplayName("GET /api/v1/market/ticks/latest/1 — permitAll, 200")
    void marketPath_isPermittedWithoutToken() throws Exception {
        mvc.perform(get("/api/v1/market/ticks/latest/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("market-ok"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me without token — 401")
    void authMe_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with valid Bearer — 200")
    void authMe_withValidToken_returns200() throws Exception {
        givenValidToken("valid-token");
        mvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("auth-me-ok"));
    }

    @Test
    @DisplayName("GET /api/v1/watchlist without token — 401")
    void watchlist_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/watchlist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/watchlist with ApiKey — 403 (JWT account only)")
    void watchlist_withApiKey_returns403() throws Exception {
        givenValidApiKey("valid-api-key");
        mvc.perform(get("/api/v1/watchlist")
                        .header("Authorization", "ApiKey valid-api-key"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/alert/rules without token — 401")
    void alertRules_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/alert/rules"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/stream/ticks?access_token=valid — 200 (SSE fallback)")
    void streamTicks_withValidQueryToken_returns200() throws Exception {
        givenValidToken("valid-token");
        mvc.perform(get("/api/v1/stream/ticks")
                        .param("access_token", "valid-token")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().string("stream-ok"));
    }

    @Test
    @DisplayName("GET /api/v1/stream/ticks without token — 200 (public stream)")
    void streamTicks_withoutToken_returns200() throws Exception {
        mvc.perform(get("/api/v1/stream/ticks")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().string("stream-ok"));
    }

    @Test
    @DisplayName("GET /api/v1/stream/alerts without token — 401")
    void alertStream_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/stream/alerts")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/stream/alerts?access_token=valid — 200 (private JWT stream)")
    void alertStream_withValidQueryToken_returns200() throws Exception {
        givenValidToken("valid-token");
        mvc.perform(get("/api/v1/stream/alerts")
                        .param("access_token", "valid-token")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().string("alert-stream-ok"));
    }

    @Test
    @DisplayName("GET /api/v1/stream/ticks?access_token=invalid — 401")
    void streamTicks_withInvalidQueryToken_returns401() throws Exception {
        given(verifyAccessTokenUseCase.verify(any(), any())).willReturn(Optional.empty());
        mvc.perform(get("/api/v1/stream/ticks")
                        .param("access_token", "expired")
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isUnauthorized());
    }

    private void givenValidToken(String token) {
        AuthenticatedAccount account = new AuthenticatedAccount(
                AccountId.of(UUID.randomUUID()),
                new Email("slice@example.com"),
                AccountTier.FREE
        );
        given(verifyAccessTokenUseCase.verify(eq("Bearer " + token), any(Instant.class)))
                .willReturn(Optional.of(account));
    }

    private void givenValidApiKey(String token) {
        AuthenticatedApiKey apiKey = new AuthenticatedApiKey(
                ApiKeyId.generate(),
                AccountId.of(UUID.randomUUID()),
                AccountTier.PRO,
                Set.of(ApiKeyScope.READ_PRIVATE),
                new RateLimitPolicy(120, 100_000, 4)
        );
        given(authenticateApiKeyUseCase.authenticate(eq("ApiKey " + token), any(), any(Instant.class)))
                .willReturn(Optional.of(apiKey));
    }

    @RestController
    @RequestMapping("/api/v1/market/ticks")
    static class StubPublicController {
        @GetMapping("/latest/{id}")
        public String latest(@org.springframework.web.bind.annotation.PathVariable long id) {
            return "market-ok";
        }
    }

    @RestController
    static class StubProtectedController {
        @GetMapping("/api/v1/watchlist")
        public String watchlist() {
            return "watchlist-ok";
        }

        @GetMapping("/api/v1/alert/rules")
        public String alertRules() {
            return "alert-rules-ok";
        }
    }

    @RestController
    @RequestMapping("/api/v1/auth")
    static class StubAuthMeController {
        @GetMapping("/me")
        public String me() {
            return "auth-me-ok";
        }
    }

    @RestController
    @RequestMapping("/api/v1/stream")
    static class StubStreamController {
        @GetMapping("/ticks")
        public String ticks() {
            return "stream-ok";
        }

        @GetMapping("/alerts")
        public String alerts() {
            return "alert-stream-ok";
        }
    }
}
