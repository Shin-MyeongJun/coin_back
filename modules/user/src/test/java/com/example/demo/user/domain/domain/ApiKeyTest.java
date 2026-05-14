package com.example.demo.user.domain.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeyTest {

    private static ApiKey newKey(Set<String> ips) {
        return ApiKey.issue(
                ApiKeyId.generate(),
                AccountId.generate(),
                "label",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET),
                ips,
                RateLimitPolicies.FREE,
                Instant.parse("2026-05-14T00:00:00Z")
        );
    }

    @Test
    void issue_rejects_blank_label_and_empty_scopes() {
        assertThatThrownBy(() -> ApiKey.issue(
                ApiKeyId.generate(), AccountId.generate(), "  ",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET), Set.of(),
                RateLimitPolicies.FREE, Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> ApiKey.issue(
                ApiKeyId.generate(), AccountId.generate(), "x",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.noneOf(ApiKeyScope.class), Set.of(),
                RateLimitPolicies.FREE, Instant.now()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void revoke_sets_revokedAt_and_isActive_returns_false() {
        ApiKey k = newKey(Set.of());
        Instant t = Instant.parse("2026-05-14T01:00:00Z");
        assertThat(k.isActive(t)).isTrue();
        k.revoke(t);
        assertThat(k.getRevokedAt()).isEqualTo(t);
        assertThat(k.isActive(t)).isFalse();
    }

    @Test
    void revoke_is_idempotent() {
        ApiKey k = newKey(Set.of());
        Instant t1 = Instant.parse("2026-05-14T01:00:00Z");
        Instant t2 = t1.plusSeconds(60);
        k.revoke(t1);
        k.revoke(t2);
        assertThat(k.getRevokedAt()).isEqualTo(t1);
    }

    @Test
    void empty_ipAllowlist_means_no_restriction() {
        ApiKey k = newKey(Set.of());
        assertThat(k.isIpAllowed("1.2.3.4")).isTrue();
        assertThat(k.isIpAllowed(null)).isTrue();
    }

    @Test
    void non_empty_ipAllowlist_enforces_membership() {
        ApiKey k = newKey(new LinkedHashSet<>(Set.of("10.0.0.1", "10.0.0.2")));
        assertThat(k.isIpAllowed("10.0.0.1")).isTrue();
        assertThat(k.isIpAllowed("10.0.0.99")).isFalse();
        assertThat(k.isIpAllowed(null)).isFalse();
    }

    @Test
    void hasScope_checks_membership() {
        ApiKey k = newKey(Set.of());
        assertThat(k.hasScope(ApiKeyScope.READ_MARKET)).isTrue();
        assertThat(k.hasScope(ApiKeyScope.READ_PRIVATE)).isFalse();
    }
}
