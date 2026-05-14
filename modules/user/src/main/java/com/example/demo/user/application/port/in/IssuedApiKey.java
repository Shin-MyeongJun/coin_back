package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeySecret;

import java.util.Objects;

/**
 * Returned by {@link IssueApiKeyUseCase} so the controller can expose the plaintext secret
 * exactly once (it is not stored).
 */
public record IssuedApiKey(ApiKey apiKey, ApiKeySecret secret) {
    public IssuedApiKey {
        Objects.requireNonNull(apiKey, "apiKey");
        Objects.requireNonNull(secret, "secret");
    }
}
