package com.example.demo.user.infrastructure.web;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.IssueApiKeyUseCase;
import com.example.demo.user.application.port.in.IssuedApiKey;
import com.example.demo.user.application.port.in.ListApiKeysQuery;
import com.example.demo.user.application.port.in.RevokeApiKeyUseCase;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.ApiKeySecret;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.RateLimitPolicies;
import com.example.demo.user.domain.exception.ApiKeyNotFoundException;
import com.example.demo.user.domain.exception.ApiKeyOwnershipException;
import com.example.demo.user.domain.exception.ApiKeyQuotaExceededException;
import com.example.demo.user.infrastructure.web.error.UserErrorAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserErrorAdvice.class)
class ApiKeyControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean IssueApiKeyUseCase issueApiKeyUseCase;
    @MockitoBean RevokeApiKeyUseCase revokeApiKeyUseCase;
    @MockitoBean ListApiKeysQuery listApiKeysQuery;

    private AccountId accountId;

    @BeforeEach
    void setup() {
        accountId = AccountId.generate();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor authenticated() {
        AuthenticatedAccount account = new AuthenticatedAccount(accountId, Email.of("a@b.com"), AccountTier.PRO);
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(account, null, "ROLE_USER"));
            return request;
        };
    }

    @Test
    void issue_returns_201_with_plaintext_secret_exactly_once() throws Exception {
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        ApiKey k = ApiKey.issue(
                ApiKeyId.generate(), accountId, "trading-bot",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET),
                java.util.Set.of(),
                RateLimitPolicies.PRO,
                now
        );
        IssuedApiKey issued = new IssuedApiKey(k, new ApiKeySecret("S".repeat(32)));
        given(issueApiKeyUseCase.issue(any(), any(), any(), any(), any())).willReturn(issued);

        mvc.perform(post("/api/v1/api-keys")
                        .with(authenticated())
                        .header("Authorization", "Bearer AT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "label", "trading-bot",
                                "scopes", List.of("READ_MARKET")
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prefix").value("ABCD1234"))
                .andExpect(jsonPath("$.secret").value("S".repeat(32)))
                .andExpect(jsonPath("$.label").value("trading-bot"))
                .andExpect(jsonPath("$.policy.rpm").value(600))
                .andExpect(jsonPath("$.policy.rpd").value(200_000))
                .andExpect(jsonPath("$.policy.sseConcurrent").value(5))
                .andExpect(jsonPath("$.hash").doesNotExist());
    }

    @Test
    void issue_returns_409_when_quota_exceeded() throws Exception {
        given(issueApiKeyUseCase.issue(any(), any(), any(), any(), any()))
                .willThrow(new ApiKeyQuotaExceededException(AccountTier.FREE, 3, 3));

        mvc.perform(post("/api/v1/api-keys")
                        .with(authenticated())
                        .header("Authorization", "Bearer AT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "label", "x",
                                "scopes", List.of("READ_MARKET")
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    void list_returns_summaries_without_hash_or_secret() throws Exception {
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        ApiKey k = ApiKey.issue(
                ApiKeyId.generate(), accountId, "trading-bot",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET),
                java.util.Set.of(),
                RateLimitPolicies.PRO,
                now
        );
        given(listApiKeysQuery.list(any())).willReturn(List.of(k));

        mvc.perform(get("/api/v1/api-keys")
                        .with(authenticated())
                        .header("Authorization", "Bearer AT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prefix").value("ABCD1234"))
                .andExpect(jsonPath("$[0].label").value("trading-bot"))
                .andExpect(jsonPath("$[0].secret").doesNotExist())
                .andExpect(jsonPath("$[0].hash").doesNotExist());
    }

    @Test
    void revoke_returns_204() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/v1/api-keys/" + id)
                        .with(authenticated())
                        .header("Authorization", "Bearer AT"))
                .andExpect(status().isNoContent());
    }

    @Test
    void revoke_returns_403_on_ownership_violation() throws Exception {
        UUID id = UUID.randomUUID();
        ApiKeyId apiKeyId = ApiKeyId.of(id);
        willThrow(new ApiKeyOwnershipException(accountId, apiKeyId))
                .given(revokeApiKeyUseCase).revoke(any(), any(), any());

        mvc.perform(delete("/api/v1/api-keys/" + id)
                        .with(authenticated())
                        .header("Authorization", "Bearer AT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void revoke_returns_404_when_missing() throws Exception {
        UUID id = UUID.randomUUID();
        ApiKeyId apiKeyId = ApiKeyId.of(id);
        willThrow(new ApiKeyNotFoundException(apiKeyId))
                .given(revokeApiKeyUseCase).revoke(any(), any(), any());

        mvc.perform(delete("/api/v1/api-keys/" + id)
                        .with(authenticated())
                        .header("Authorization", "Bearer AT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void issue_returns_401_when_no_auth() throws Exception {
        mvc.perform(post("/api/v1/api-keys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "label", "x",
                                "scopes", List.of("READ_MARKET")
                        ))))
                .andExpect(status().isUnauthorized());
    }
}
