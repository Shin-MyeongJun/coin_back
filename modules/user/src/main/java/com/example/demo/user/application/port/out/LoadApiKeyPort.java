package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyId;

import java.util.List;
import java.util.Optional;

public interface LoadApiKeyPort {
    Optional<ApiKey> findById(ApiKeyId id);
    List<ApiKey> findByAccountId(AccountId accountId);
    long countActiveByAccountId(AccountId accountId);
}
