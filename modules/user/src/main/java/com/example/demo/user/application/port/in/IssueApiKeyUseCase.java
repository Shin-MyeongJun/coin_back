package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKeyScope;

import java.time.Instant;
import java.util.Set;

public interface IssueApiKeyUseCase {
    IssuedApiKey issue(
            AccountId accountId,
            String label,
            Set<ApiKeyScope> scopes,
            Set<String> ipAllowlist,
            Instant now
    );
}
