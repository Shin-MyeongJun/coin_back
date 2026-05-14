package com.example.demo.user.domain.service;

import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.exception.ApiKeyQuotaExceededException;
import com.example.demo.user.domain.exception.ApiKeyScopeNotAllowedException;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeyIssuancePolicyTest {

    @Test
    void quotas_match_spec() {
        assertThat(ApiKeyIssuancePolicy.maxKeys(AccountTier.FREE)).isEqualTo(3);
        assertThat(ApiKeyIssuancePolicy.maxKeys(AccountTier.PRO)).isEqualTo(20);
        assertThat(ApiKeyIssuancePolicy.maxKeys(AccountTier.ADMIN)).isEqualTo(50);
    }

    @Test
    void free_tier_allows_only_market_and_sse() {
        assertThat(ApiKeyIssuancePolicy.allowedScopes(AccountTier.FREE))
                .containsExactlyInAnyOrder(ApiKeyScope.READ_MARKET, ApiKeyScope.SSE_STREAM);
    }

    @Test
    void pro_and_admin_allow_all_scopes() {
        assertThat(ApiKeyIssuancePolicy.allowedScopes(AccountTier.PRO))
                .containsExactlyInAnyOrder(ApiKeyScope.values());
        assertThat(ApiKeyIssuancePolicy.allowedScopes(AccountTier.ADMIN))
                .containsExactlyInAnyOrder(ApiKeyScope.values());
    }

    @Test
    void validate_rejects_when_at_quota() {
        assertThatThrownBy(() -> ApiKeyIssuancePolicy.validate(
                AccountTier.FREE, EnumSet.of(ApiKeyScope.READ_MARKET), 3
        )).isInstanceOf(ApiKeyQuotaExceededException.class);
    }

    @Test
    void validate_rejects_disallowed_scope_for_tier() {
        assertThatThrownBy(() -> ApiKeyIssuancePolicy.validate(
                AccountTier.FREE, EnumSet.of(ApiKeyScope.READ_PRIVATE), 0
        )).isInstanceOf(ApiKeyScopeNotAllowedException.class);
    }

    @Test
    void validate_passes_for_allowed_scope_and_under_quota() {
        assertThatCode(() -> ApiKeyIssuancePolicy.validate(
                AccountTier.PRO, EnumSet.of(ApiKeyScope.READ_PRIVATE), 5
        )).doesNotThrowAnyException();
    }
}
