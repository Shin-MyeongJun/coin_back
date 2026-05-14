package com.example.demo.user.domain.service;

import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeySecret;

public interface ApiKeyHasher {
    ApiKeyHash hash(ApiKeySecret secret);
    boolean matches(ApiKeySecret raw, ApiKeyHash hash);
}
