package com.example.demo.user.domain.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class Account {

    private final AccountId id;
    private final Email email;
    private PasswordHash passwordHash;
    private AccountTier tier;
    private final Instant createdAt;
    private Instant updatedAt;

    public void changePassword(PasswordHash newHash, Instant now) {
        Objects.requireNonNull(newHash, "newHash must not be null");
        Objects.requireNonNull(now, "now must not be null");
        this.passwordHash = newHash;
        this.updatedAt = now;
    }

    public void upgradeTier(AccountTier newTier, Instant now) {
        Objects.requireNonNull(newTier, "newTier must not be null");
        Objects.requireNonNull(now, "now must not be null");
        this.tier = newTier;
        this.updatedAt = now;
    }
}
