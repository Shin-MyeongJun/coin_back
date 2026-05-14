package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.RevokeApiKeyUseCase;
import com.example.demo.user.application.port.out.ApiKeyLookupCachePort;
import com.example.demo.user.application.port.out.LoadApiKeyPort;
import com.example.demo.user.application.port.out.SaveApiKeyPort;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.exception.ApiKeyNotFoundException;
import com.example.demo.user.domain.exception.ApiKeyOwnershipException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RevokeApiKeyService implements RevokeApiKeyUseCase {

    private final LoadApiKeyPort loadApiKeyPort;
    private final SaveApiKeyPort saveApiKeyPort;
    private final ApiKeyLookupCachePort apiKeyLookupCachePort;

    @Override
    @Transactional
    public void revoke(AccountId callerAccountId, ApiKeyId apiKeyId, Instant now) {
        ApiKey apiKey = loadApiKeyPort.findById(apiKeyId)
                .orElseThrow(() -> new ApiKeyNotFoundException(apiKeyId));

        if (!apiKey.getAccountId().equals(callerAccountId)) {
            throw new ApiKeyOwnershipException(callerAccountId, apiKeyId);
        }

        apiKey.revoke(now);
        saveApiKeyPort.save(apiKey);
        apiKeyLookupCachePort.evict(apiKey.getPrefix());
    }
}
