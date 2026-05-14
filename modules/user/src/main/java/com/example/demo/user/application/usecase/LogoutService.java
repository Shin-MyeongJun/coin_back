package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.LogoutUseCase;
import com.example.demo.user.application.port.out.RefreshTokenStorePort;
import com.example.demo.user.application.port.out.TokenVerifierPort;
import com.example.demo.user.application.port.out.AccessTokenBlacklistPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenVerifierPort tokenVerifierPort;
    private final RefreshTokenStorePort refreshTokenStorePort;
    private final AccessTokenBlacklistPort accessTokenBlacklistPort;

    @Override
    public void logout(String accessTokenRaw, String refreshTokenRaw, Instant now) {
        // Best effort: blacklist access jti for its remaining TTL.
        tokenVerifierPort.verifyAccess(accessTokenRaw, now).ifPresent(claims ->
                accessTokenBlacklistPort.blacklist(claims.jti(), claims.expiresAt())
        );

        // Best effort: revoke refresh allowlist entry.
        if (refreshTokenRaw != null && !refreshTokenRaw.isBlank()) {
            tokenVerifierPort.verifyRefresh(refreshTokenRaw, now).ifPresent(claims ->
                    refreshTokenStorePort.revoke(claims.accountId(), claims.jti())
            );
        }
    }
}
