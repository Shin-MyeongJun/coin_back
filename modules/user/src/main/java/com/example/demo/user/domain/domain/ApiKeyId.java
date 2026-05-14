package com.example.demo.user.domain.domain;

import java.util.Objects;
import java.util.UUID;

public record ApiKeyId(UUID value) {
    public ApiKeyId {
        Objects.requireNonNull(value, "ApiKeyId.value must not be null");
    }

    public static ApiKeyId of(UUID value) { return new ApiKeyId(value); }
    public static ApiKeyId of(String value) { return new ApiKeyId(UUID.fromString(value)); }
    public static ApiKeyId generate() { return new ApiKeyId(UUID.randomUUID()); }

    public String asString() { return value.toString(); }
}
