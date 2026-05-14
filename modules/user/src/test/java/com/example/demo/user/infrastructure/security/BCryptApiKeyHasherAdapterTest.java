package com.example.demo.user.infrastructure.security;

import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeySecret;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptApiKeyHasherAdapterTest {

    @Test
    void hash_then_match_succeeds() {
        BCryptApiKeyHasherAdapter h = new BCryptApiKeyHasherAdapter();
        ApiKeySecret raw = new ApiKeySecret("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        ApiKeyHash hash = h.hash(raw);

        assertThat(hash.value()).startsWith("$2");
        assertThat(h.matches(raw, hash)).isTrue();
    }

    @Test
    void match_fails_with_different_secret() {
        BCryptApiKeyHasherAdapter h = new BCryptApiKeyHasherAdapter();
        ApiKeySecret raw1 = new ApiKeySecret("a".repeat(32));
        ApiKeySecret raw2 = new ApiKeySecret("b".repeat(32));
        ApiKeyHash hash = h.hash(raw1);
        assertThat(h.matches(raw2, hash)).isFalse();
    }

    @Test
    void match_handles_null() {
        BCryptApiKeyHasherAdapter h = new BCryptApiKeyHasherAdapter();
        ApiKeyHash hash = h.hash(new ApiKeySecret("a".repeat(32)));
        assertThat(h.matches(null, hash)).isFalse();
        assertThat(h.matches(new ApiKeySecret("a".repeat(32)), null)).isFalse();
    }
}
