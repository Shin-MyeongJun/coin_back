package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyPrefix;

import java.util.Optional;

public interface LoadApiKeyByPrefixPort {
    Optional<ApiKey> findByPrefix(ApiKeyPrefix prefix);
}
