package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.AuthenticatedApiKey;
import com.example.demo.user.application.port.out.ApiKeyLookupCachePort;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.LoadApiKeyByPrefixPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.domain.RateLimitPolicies;
import com.example.demo.user.domain.service.ApiKeyHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticateApiKeyServiceTest {

    @Mock LoadApiKeyByPrefixPort loadApiKeyByPrefixPort;
    @Mock ApiKeyLookupCachePort apiKeyLookupCachePort;
    @Mock ApiKeyHasher apiKeyHasher;
    @Mock LoadAccountPort loadAccountPort;

    @InjectMocks AuthenticateApiKeyService service;

    private static final String VALID_HEADER = "ApiKey ABCD1234.aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private AccountId accountId;
    private ApiKey apiKey;
    private Account account;

    @BeforeEach
    void setup() {
        accountId = AccountId.generate();
        Instant t = Instant.parse("2026-05-14T00:00:00Z");
        apiKey = ApiKey.issue(
                ApiKeyId.generate(), accountId, "lbl",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET),
                Set.of(),
                RateLimitPolicies.PRO,
                t
        );
        account = new Account(accountId, Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                AccountTier.PRO, t, t);
    }

    @Test
    void returns_empty_when_header_missing_or_malformed() {
        assertThat(service.authenticate(null, "1.1.1.1", Instant.now())).isEmpty();
        assertThat(service.authenticate("Bearer xxx", "1.1.1.1", Instant.now())).isEmpty();
        assertThat(service.authenticate("ApiKey nodot", "1.1.1.1", Instant.now())).isEmpty();
        assertThat(service.authenticate("ApiKey short.short", "1.1.1.1", Instant.now())).isEmpty();
    }

    @Test
    void returns_empty_when_unknown_prefix() {
        given(apiKeyLookupCachePort.get(any())).willReturn(Optional.empty());
        given(loadApiKeyByPrefixPort.findByPrefix(any())).willReturn(Optional.empty());

        assertThat(service.authenticate(VALID_HEADER, "1.1.1.1", Instant.now())).isEmpty();
        verify(apiKeyLookupCachePort, never()).put(any());
    }

    @Test
    void returns_empty_when_secret_does_not_match() {
        given(apiKeyLookupCachePort.get(any())).willReturn(Optional.empty());
        given(loadApiKeyByPrefixPort.findByPrefix(any())).willReturn(Optional.of(apiKey));
        given(apiKeyHasher.matches(any(), any())).willReturn(false);

        assertThat(service.authenticate(VALID_HEADER, "1.1.1.1", Instant.now())).isEmpty();
    }

    @Test
    void returns_empty_when_revoked() {
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        apiKey.revoke(now);

        given(apiKeyLookupCachePort.get(any())).willReturn(Optional.of(apiKey));
        given(apiKeyHasher.matches(any(), any())).willReturn(true);

        assertThat(service.authenticate(VALID_HEADER, "1.1.1.1", now)).isEmpty();
    }

    @Test
    void returns_empty_when_ip_not_allowed() {
        ApiKey ipRestricted = ApiKey.issue(
                ApiKeyId.generate(), accountId, "lbl",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET),
                new LinkedHashSet<>(Set.of("10.0.0.1")),
                RateLimitPolicies.PRO,
                Instant.parse("2026-05-14T00:00:00Z")
        );

        given(apiKeyLookupCachePort.get(any())).willReturn(Optional.of(ipRestricted));
        given(apiKeyHasher.matches(any(), any())).willReturn(true);

        assertThat(service.authenticate(VALID_HEADER, "9.9.9.9", Instant.parse("2026-05-14T00:01:00Z")))
                .isEmpty();
    }

    @Test
    void returns_authenticated_on_cache_hit_and_does_not_repopulate() {
        Instant now = Instant.parse("2026-05-14T00:01:00Z");
        given(apiKeyLookupCachePort.get(any())).willReturn(Optional.of(apiKey));
        given(apiKeyHasher.matches(any(), any())).willReturn(true);
        given(loadAccountPort.findById(accountId)).willReturn(Optional.of(account));

        Optional<AuthenticatedApiKey> result = service.authenticate(VALID_HEADER, "1.1.1.1", now);

        assertThat(result).isPresent();
        assertThat(result.get().tier()).isEqualTo(AccountTier.PRO);
        assertThat(result.get().scopes()).containsExactly(ApiKeyScope.READ_MARKET);
        verify(apiKeyLookupCachePort, never()).put(any());
    }

    @Test
    void populates_cache_on_db_hit() {
        Instant now = Instant.parse("2026-05-14T00:01:00Z");
        given(apiKeyLookupCachePort.get(any())).willReturn(Optional.empty());
        given(loadApiKeyByPrefixPort.findByPrefix(any())).willReturn(Optional.of(apiKey));
        given(apiKeyHasher.matches(any(), any())).willReturn(true);
        given(loadAccountPort.findById(accountId)).willReturn(Optional.of(account));

        Optional<AuthenticatedApiKey> result = service.authenticate(VALID_HEADER, "1.1.1.1", now);

        assertThat(result).isPresent();
        verify(apiKeyLookupCachePort).put(apiKey);
    }
}
