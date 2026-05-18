package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.IssuedApiKey;
import com.example.demo.user.application.port.out.ApiKeyLookupCachePort;
import com.example.demo.user.application.port.out.ApiKeySecretGeneratorPort;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.LoadApiKeyByPrefixPort;
import com.example.demo.user.application.port.out.LoadApiKeyPort;
import com.example.demo.user.application.port.out.SaveApiKeyPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.ApiKeySecret;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.domain.PrefixAndSecret;
import com.example.demo.user.domain.exception.ApiKeyQuotaExceededException;
import com.example.demo.user.domain.exception.ApiKeyScopeNotAllowedException;
import com.example.demo.user.domain.service.ApiKeyHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IssueApiKeyServiceTest {

    @Mock LoadAccountPort loadAccountPort;
    @Mock LoadApiKeyPort loadApiKeyPort;
    @Mock LoadApiKeyByPrefixPort loadApiKeyByPrefixPort;
    @Mock SaveApiKeyPort saveApiKeyPort;
    @Mock ApiKeyHasher apiKeyHasher;
    @Mock ApiKeySecretGeneratorPort apiKeySecretGeneratorPort;
    @Mock ApiKeyLookupCachePort apiKeyLookupCachePort;

    @InjectMocks IssueApiKeyService service;

    private Account account(AccountTier tier) {
        Instant t = Instant.parse("2026-05-14T00:00:00Z");
        return new Account(
                AccountId.generate(),
                Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                tier, t, t
        );
    }

    @Test
    void rejects_when_quota_exceeded() {
        Account a = account(AccountTier.FREE);
        given(loadAccountPort.findByIdForUpdate(a.getId())).willReturn(Optional.of(a));
        given(loadApiKeyPort.countActiveByAccountId(a.getId())).willReturn(3L);

        assertThatThrownBy(() -> service.issue(
                a.getId(), "lbl",
                EnumSet.of(ApiKeyScope.READ_MARKET), Set.of(), Instant.now()
        )).isInstanceOf(ApiKeyQuotaExceededException.class);

        verify(saveApiKeyPort, never()).save(any());
    }

    @Test
    void rejects_when_scope_not_allowed_for_tier() {
        Account a = account(AccountTier.FREE);
        given(loadAccountPort.findByIdForUpdate(a.getId())).willReturn(Optional.of(a));
        given(loadApiKeyPort.countActiveByAccountId(a.getId())).willReturn(0L);

        assertThatThrownBy(() -> service.issue(
                a.getId(), "lbl",
                EnumSet.of(ApiKeyScope.READ_PRIVATE), Set.of(), Instant.now()
        )).isInstanceOf(ApiKeyScopeNotAllowedException.class);

        verify(saveApiKeyPort, never()).save(any());
    }

    @Test
    void issues_when_allowed_and_caches() {
        Account a = account(AccountTier.PRO);
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        PrefixAndSecret credentials = new PrefixAndSecret(
                ApiKeyPrefix.of("ABCD1234"),
                new ApiKeySecret("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        );
        ApiKeyHash hash = ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA");

        given(loadAccountPort.findByIdForUpdate(a.getId())).willReturn(Optional.of(a));
        given(loadApiKeyPort.countActiveByAccountId(a.getId())).willReturn(0L);
        given(apiKeySecretGeneratorPort.generate()).willReturn(credentials);
        given(loadApiKeyByPrefixPort.findByPrefix(credentials.prefix())).willReturn(Optional.empty());
        given(apiKeyHasher.hash(credentials.secret())).willReturn(hash);
        given(saveApiKeyPort.save(any())).willAnswer(inv -> inv.getArgument(0));

        IssuedApiKey out = service.issue(
                a.getId(), "my-key",
                EnumSet.of(ApiKeyScope.READ_PRIVATE),
                Set.of("10.0.0.1"),
                now
        );

        assertThat(out.secret()).isEqualTo(credentials.secret());
        assertThat(out.apiKey().getPrefix()).isEqualTo(credentials.prefix());
        assertThat(out.apiKey().getHash()).isEqualTo(hash);
        assertThat(out.apiKey().hasScope(ApiKeyScope.READ_PRIVATE)).isTrue();
        verify(apiKeyLookupCachePort).put(any());
    }

    @Test
    void retries_on_prefix_collision() {
        Account a = account(AccountTier.PRO);
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        PrefixAndSecret first = new PrefixAndSecret(ApiKeyPrefix.of("AAAAAAAA"), new ApiKeySecret("a".repeat(32)));
        PrefixAndSecret second = new PrefixAndSecret(ApiKeyPrefix.of("BBBBBBBB"), new ApiKeySecret("b".repeat(32)));
        ApiKeyHash hash = ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA");
        com.example.demo.user.domain.domain.ApiKey existing = com.example.demo.user.domain.domain.ApiKey.issue(
                com.example.demo.user.domain.domain.ApiKeyId.generate(),
                a.getId(), "existing",
                first.prefix(), hash,
                EnumSet.of(ApiKeyScope.READ_MARKET),
                Set.of(),
                com.example.demo.user.domain.domain.RateLimitPolicies.PRO,
                now
        );

        given(loadAccountPort.findByIdForUpdate(a.getId())).willReturn(Optional.of(a));
        given(loadApiKeyPort.countActiveByAccountId(a.getId())).willReturn(0L);
        given(apiKeySecretGeneratorPort.generate()).willReturn(first, second);
        given(loadApiKeyByPrefixPort.findByPrefix(first.prefix())).willReturn(Optional.of(existing));
        given(loadApiKeyByPrefixPort.findByPrefix(second.prefix())).willReturn(Optional.empty());
        given(apiKeyHasher.hash(second.secret())).willReturn(hash);
        given(saveApiKeyPort.save(any())).willAnswer(inv -> inv.getArgument(0));

        IssuedApiKey out = service.issue(
                a.getId(), "k",
                EnumSet.of(ApiKeyScope.READ_MARKET),
                Set.of(), now
        );

        assertThat(out.apiKey().getPrefix()).isEqualTo(second.prefix());
    }
}
