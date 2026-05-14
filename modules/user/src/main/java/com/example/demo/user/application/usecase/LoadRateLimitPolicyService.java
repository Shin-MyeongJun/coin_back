package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.LoadRateLimitPolicyQuery;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.LoadApiKeyPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.RateLimitPolicies;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.example.demo.user.domain.exception.AccountNotFoundException;
import com.example.demo.user.domain.exception.ApiKeyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoadRateLimitPolicyService implements LoadRateLimitPolicyQuery {

    private final LoadAccountPort loadAccountPort;
    private final LoadApiKeyPort loadApiKeyPort;

    @Override
    @Transactional(readOnly = true)
    public RateLimitPolicy forAccount(AccountId accountId) {
        Account account = loadAccountPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return RateLimitPolicies.forTier(account.getTier());
    }

    @Override
    @Transactional(readOnly = true)
    public RateLimitPolicy forApiKey(ApiKeyId apiKeyId) {
        ApiKey apiKey = loadApiKeyPort.findById(apiKeyId)
                .orElseThrow(() -> new ApiKeyNotFoundException(apiKeyId));
        return apiKey.getPolicy();
    }
}
