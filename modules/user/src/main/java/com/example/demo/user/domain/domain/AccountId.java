package com.example.demo.user.domain.domain;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {
    public AccountId {
        Objects.requireNonNull(value, "AccountId.value must not be null");
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

    public static AccountId of(String value) {
        return new AccountId(UUID.fromString(value));
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }

    public String asString() {
        return value.toString();
    }
}
