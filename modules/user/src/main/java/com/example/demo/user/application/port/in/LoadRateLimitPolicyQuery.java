package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.RateLimitPolicy;

public interface LoadRateLimitPolicyQuery {
    RateLimitPolicy forAccount(AccountId accountId);
    RateLimitPolicy forApiKey(ApiKeyId apiKeyId);
}
