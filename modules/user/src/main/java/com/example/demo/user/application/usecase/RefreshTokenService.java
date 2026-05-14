package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.RefreshTokenUseCase;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.RefreshTokenStorePort;
import com.example.demo.user.application.port.out.TokenIssuerPort;
import com.example.demo.user.application.port.out.TokenVerifierPort;
import com.example.demo.user.application.port.out.VerifiedClaims;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.TokenPair;
import com.example.demo.user.domain.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final TokenVerifierPort tokenVerifierPort;
    private final TokenIssuerPort tokenIssuerPort;
    private final RefreshTokenStorePort refreshTokenStorePort;
    private final LoadAccountPort loadAccountPort;

    @Override
    public TokenPair refresh(String refreshTokenRaw, Instant now) {
        VerifiedClaims claims = tokenVerifierPort.verifyRefresh(refreshTokenRaw, now)
                .orElseThrow(() -> new TokenInvalidException("refresh token invalid or expired"));

        if (!refreshTokenStorePort.isAllowed(claims.accountId(), claims.jti())) {
            throw new TokenInvalidException("refresh token not in allowlist (revoked or rotated)");
        }

        Account account = loadAccountPort.findById(claims.accountId())
                .orElseThrow(() -> new TokenInvalidException("account no longer exists"));

        // rotation: revoke old jti, issue new pair, allow new refresh jti
        refreshTokenStorePort.revoke(claims.accountId(), claims.jti());

        TokenPair pair = tokenIssuerPort.issue(account.getId(), account.getTier(), now);
        refreshTokenStorePort.allow(
                pair.refresh().accountId(),
                pair.refresh().jti(),
                pair.refresh().expiresAt()
        );
        return pair;
    }
}
