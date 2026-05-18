package com.example.demo.user.infrastructure.web;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.LoginUseCase;
import com.example.demo.user.application.port.in.LogoutUseCase;
import com.example.demo.user.application.port.in.RefreshTokenUseCase;
import com.example.demo.user.application.port.in.SignupUseCase;
import com.example.demo.user.domain.domain.AccessToken;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.domain.RefreshToken;
import com.example.demo.user.domain.domain.TokenPair;
import com.example.demo.user.domain.exception.DuplicateEmailException;
import com.example.demo.user.infrastructure.security.JwtProperties;
import com.example.demo.user.infrastructure.web.error.UserErrorAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserErrorAdvice.class, AuthControllerTest.TestJwtConfig.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean SignupUseCase signupUseCase;
    @MockitoBean LoginUseCase loginUseCase;
    @MockitoBean RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean LogoutUseCase logoutUseCase;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestJwtConfig {
        @org.springframework.context.annotation.Bean
        JwtProperties jwtProperties() {
            return new JwtProperties("0123456789012345678901234567890123456789012345678901234567890123", 300, 1209600, "ys-coin");
        }
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor authenticated(AuthenticatedAccount account, String rawToken) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(account, rawToken, "ROLE_USER"));
            return request;
        };
    }

    @Test
    void signup_returns_201() throws Exception {
        Account a = new Account(
                AccountId.generate(),
                Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                AccountTier.FREE,
                Instant.parse("2026-05-14T00:00:00Z"),
                Instant.parse("2026-05-14T00:00:00Z")
        );
        given(signupUseCase.signup(any(), any(), any())).willReturn(a);

        mvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "a@b.com", "password", "password1234"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("a@b.com"))
                .andExpect(jsonPath("$.tier").value("FREE"));
    }

    @Test
    void signup_duplicate_returns_409() throws Exception {
        given(signupUseCase.signup(any(), any(), any())).willThrow(new DuplicateEmailException("a@b.com"));

        mvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "a@b.com", "password", "password1234"))))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns_200_with_refresh_cookie() throws Exception {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        TokenPair pair = new TokenPair(
                new AccessToken("AT", "access-jti", now.plusSeconds(300)),
                new RefreshToken("RT", "refresh-jti", id, now.plusSeconds(1209600))
        );
        given(loginUseCase.login(any(), any(), any())).willReturn(pair);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("email", "a@b.com", "password", "password1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("AT"))
                .andExpect(cookie().httpOnly("refresh", true))
                .andExpect(cookie().value("refresh", "RT"))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));
    }

    @Test
    void me_returns_200_when_verified() throws Exception {
        AccountId id = AccountId.generate();
        AuthenticatedAccount me = new AuthenticatedAccount(id, Email.of("a@b.com"), AccountTier.FREE);

        mvc.perform(get("/api/v1/auth/me")
                        .with(authenticated(me, "AT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@b.com"))
                .andExpect(jsonPath("$.tier").value("FREE"));
    }

    @Test
    void me_returns_401_when_missing_token() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_returns_204_and_clears_cookie() throws Exception {
        AuthenticatedAccount me = new AuthenticatedAccount(AccountId.generate(), Email.of("a@b.com"), AccountTier.FREE);

        mvc.perform(post("/api/v1/auth/logout")
                        .with(authenticated(me, "AT"))
                        .cookie(new jakarta.servlet.http.Cookie("refresh", "RT")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh=")))
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
