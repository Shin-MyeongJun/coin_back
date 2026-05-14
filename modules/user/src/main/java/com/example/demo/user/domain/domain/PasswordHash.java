package com.example.demo.user.domain.domain;

import java.util.Objects;

public record PasswordHash(String value) {

    public PasswordHash {
        Objects.requireNonNull(value, "PasswordHash.value must not be null");
        if (!value.startsWith("$2")) {
            throw new IllegalArgumentException("PasswordHash must be a bcrypt hash");
        }
    }

    public static PasswordHash of(String bcryptHash) {
        return new PasswordHash(bcryptHash);
    }
}
