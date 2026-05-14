package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.IssueApiKeyUseCase;
import com.example.demo.user.application.port.in.IssuedApiKey;
import com.example.demo.user.application.port.out.ApiKeyLookupCachePort;
import com.example.demo.user.application.port.out.ApiKeySecretGeneratorPort;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.LoadApiKeyByPrefixPort;
import com.example.demo.user.application.port.out.LoadApiKeyPort;
import com.example.demo.user.application.port.out.SaveApiKeyPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.PrefixAndSecret;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.example.demo.user.domain.exception.AccountNotFoundException;
import com.example.demo.user.domain.service.ApiKeyHasher;
import com.example.demo.user.domain.service.ApiKeyIssuancePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IssueApiKeyService implements IssueApiKeyUseCase {

    private static final int MAX_PREFIX_COLLISION_RETRIES = 5;

    private final LoadAccountPort loadAccountPort;
    private final LoadApiKeyPort loadApiKeyPort;
    private final LoadApiKeyByPrefixPort loadApiKeyByPrefixPort;
    private final SaveApiKeyPort saveApiKeyPort;
    private final ApiKeyHasher apiKeyHasher;
    private final ApiKeySecretGeneratorPort apiKeySecretGeneratorPort;
    private final ApiKeyLookupCachePort apiKeyLookupCachePort;

    @Override
    @Transactional
    public IssuedApiKey issue(
            AccountId accountId,
            String label,
            Set<ApiKeyScope> scopes,
            Set<String> ipAllowlist,
            Instant now
    ) {
        Account account = loadAccountPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        long activeCount = loadApiKeyPort.countActiveByAccountId(accountId);
        ApiKeyIssuancePolicy.validate(account.getTier(), scopes, activeCount);

        RateLimitPolicy policy = ApiKeyIssuancePolicy.defaultPolicy(account.getTier());

        PrefixAndSecret credentials = generateUniqueCredentials();
        ApiKeyHash hash = apiKeyHasher.hash(credentials.secret());

        ApiKey apiKey = ApiKey.issue(
                ApiKeyId.generate(),
                accountId,
                label,
                credentials.prefix(),
                hash,
                scopes,
                ipAllowlist,
                policy,
                now
        );

        ApiKey saved = saveApiKeyPort.save(apiKey);
        apiKeyLookupCachePort.put(saved);
        return new IssuedApiKey(saved, credentials.secret());
    }

    private PrefixAndSecret generateUniqueCredentials() {
        for (int attempt = 0; attempt < MAX_PREFIX_COLLISION_RETRIES; attempt++) {
            PrefixAndSecret candidate = apiKeySecretGeneratorPort.generate();
            if (loadApiKeyByPrefixPort.findByPrefix(candidate.prefix()).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate a unique ApiKey prefix after "
                + MAX_PREFIX_COLLISION_RETRIES + " attempts");
    }
}
