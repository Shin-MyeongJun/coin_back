package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.TokenPair;

import java.time.Instant;

public interface TokenIssuerPort {
    TokenPair issue(AccountId accountId, AccountTier tier, Instant now);
}
