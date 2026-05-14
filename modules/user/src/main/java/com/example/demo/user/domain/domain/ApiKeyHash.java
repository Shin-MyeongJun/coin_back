package com.example.demo.user.domain.domain;

import java.util.Objects;

public record ApiKeyHash(String value) {

    public ApiKeyHash {
        Objects.requireNonNull(value, "ApiKeyHash.value must not be null");
        if (!value.startsWith("$2")) {
            throw new IllegalArgumentException("ApiKeyHash must be a bcrypt hash");
        }
    }

    public static ApiKeyHash of(String bcryptHash) { return new ApiKeyHash(bcryptHash); }
}
