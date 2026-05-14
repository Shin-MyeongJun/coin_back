package com.example.demo.user.domain.domain;

import com.example.demo.user.domain.service.AccountFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    void changePassword_updates_hash_and_updatedAt() {
        Instant t0 = Instant.parse("2026-05-14T00:00:00Z");
        Instant t1 = t0.plusSeconds(60);
        Account a = AccountFactory.create(
                Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                t0
        );
        PasswordHash newHash = PasswordHash.of("$2a$12$BBBBBBBBBBBBBBBBBBBBBB");

        a.changePassword(newHash, t1);

        assertThat(a.getPasswordHash()).isEqualTo(newHash);
        assertThat(a.getUpdatedAt()).isEqualTo(t1);
        assertThat(a.getCreatedAt()).isEqualTo(t0);
    }

    @Test
    void upgradeTier_updates_tier_and_updatedAt() {
        Instant t0 = Instant.parse("2026-05-14T00:00:00Z");
        Account a = AccountFactory.create(
                Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                t0
        );

        a.upgradeTier(AccountTier.PRO, t0.plusSeconds(1));

        assertThat(a.getTier()).isEqualTo(AccountTier.PRO);
    }
}
