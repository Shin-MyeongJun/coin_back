package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyPrefix;

import java.util.Optional;

public interface ApiKeyLookupCachePort {
    Optional<ApiKey> get(ApiKeyPrefix prefix);
    void put(ApiKey apiKey);
    void evict(ApiKeyPrefix prefix);
}
