package com.example.demo.user.domain.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Plaintext API key secret. MUST never be persisted; exposed only at issuance time
 * (one-time response). For storage, hash via {@code com.example.demo.user.domain.service.ApiKeyHasher}.
 */
public record ApiKeySecret(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9]{32}$");

    public ApiKeySecret {
        Objects.requireNonNull(value, "ApiKeySecret.value must not be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("ApiKeySecret must be 32 alphanumeric chars");
        }
    }
}
