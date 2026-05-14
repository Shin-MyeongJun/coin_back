package com.example.demo.user.domain.domain;

import java.util.Objects;

public record PrefixAndSecret(ApiKeyPrefix prefix, ApiKeySecret secret) {
    public PrefixAndSecret {
        Objects.requireNonNull(prefix, "PrefixAndSecret.prefix must not be null");
        Objects.requireNonNull(secret, "PrefixAndSecret.secret must not be null");
    }
}
