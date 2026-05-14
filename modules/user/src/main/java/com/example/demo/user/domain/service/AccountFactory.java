package com.example.demo.user.domain.service;

import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;

import java.time.Instant;

public final class AccountFactory {

    private AccountFactory() {}

    public static Account create(Email email, PasswordHash passwordHash, Instant now) {
        return new Account(
                AccountId.generate(),
                email,
                passwordHash,
                AccountTier.FREE,
                now,
                now
        );
    }
}
