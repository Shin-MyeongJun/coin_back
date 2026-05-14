package com.example.demo.user.domain.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
    );

    public Email {
        Objects.requireNonNull(value, "Email.value must not be null");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email of(String raw) {
        Objects.requireNonNull(raw, "Email raw must not be null");
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return new Email(normalized);
    }
}
