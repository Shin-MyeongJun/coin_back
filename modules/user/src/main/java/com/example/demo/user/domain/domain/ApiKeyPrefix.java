package com.example.demo.user.domain.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public record ApiKeyPrefix(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9]{8}$");

    public ApiKeyPrefix {
        Objects.requireNonNull(value, "ApiKeyPrefix.value must not be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("ApiKeyPrefix must be 8 alphanumeric chars");
        }
    }

    public static ApiKeyPrefix of(String value) { return new ApiKeyPrefix(value); }
}
