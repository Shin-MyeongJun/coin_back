package com.example.demo.user.domain.domain;

import java.util.Objects;

public record TokenPair(AccessToken access, RefreshToken refresh) {
    public TokenPair {
        Objects.requireNonNull(access, "TokenPair.access must not be null");
        Objects.requireNonNull(refresh, "TokenPair.refresh must not be null");
    }
}
