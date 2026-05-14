package com.example.demo.user.infrastructure.security;

import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeySecret;
import com.example.demo.user.domain.service.ApiKeyHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptApiKeyHasherAdapter implements ApiKeyHasher {

    private static final int STRENGTH = 12;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);

    @Override
    public ApiKeyHash hash(ApiKeySecret secret) {
        return ApiKeyHash.of(encoder.encode(secret.value()));
    }

    @Override
    public boolean matches(ApiKeySecret raw, ApiKeyHash hash) {
        if (raw == null || hash == null) return false;
        return encoder.matches(raw.value(), hash.value());
    }
}
