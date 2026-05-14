package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKeyId;

import java.time.Instant;

public interface RevokeApiKeyUseCase {
    void revoke(AccountId callerAccountId, ApiKeyId apiKeyId, Instant now);
}
