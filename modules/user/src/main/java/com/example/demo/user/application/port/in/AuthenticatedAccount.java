package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;

import java.util.Objects;

public record AuthenticatedAccount(AccountId id, Email email, AccountTier tier) {
    public AuthenticatedAccount {
        Objects.requireNonNull(id, "AuthenticatedAccount.id must not be null");
        Objects.requireNonNull(email, "AuthenticatedAccount.email must not be null");
        Objects.requireNonNull(tier, "AuthenticatedAccount.tier must not be null");
    }
}
