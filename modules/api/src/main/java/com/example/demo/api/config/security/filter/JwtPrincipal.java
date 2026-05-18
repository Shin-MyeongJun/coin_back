package com.example.demo.api.config.security.filter;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;

import java.util.Objects;

/**
 * JWT(access token) 기반 인증 주체.
 *
 * <p>NOTE: 현재 {@code VerifyAccessTokenUseCase} 는
 * {@code AuthenticatedAccount(id, email, tier)} 를 반환하지만,
 * principal 은 보안 결정에 필요한 식별자(accountId)와 tier 만 보유한다.
 * Email/jti 는 02_PROJECT_CONTEXT §12 시그니처 변경 금지 원칙에 따라
 * 별도 use case overload 도입 시점까지 의도적으로 제외했다.
 */
public record JwtPrincipal(AccountId accountId, AccountTier tier)
        implements AuthenticationPrincipal {

    public JwtPrincipal {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(tier, "tier");
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
