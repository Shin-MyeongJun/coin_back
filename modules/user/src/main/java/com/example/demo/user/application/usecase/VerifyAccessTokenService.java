package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.example.demo.user.application.port.out.AccessTokenBlacklistPort;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.TokenVerifierPort;
import com.example.demo.user.application.port.out.VerifiedClaims;
import com.example.demo.user.domain.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerifyAccessTokenService implements VerifyAccessTokenUseCase {

    private final TokenVerifierPort tokenVerifierPort;
    private final AccessTokenBlacklistPort accessTokenBlacklistPort;
    private final LoadAccountPort loadAccountPort;

    @Override
    public Optional<AuthenticatedAccount> verify(String bearerToken, Instant now) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return Optional.empty();
        }
        String raw = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring("Bearer ".length()).trim()
                : bearerToken.trim();

        Optional<VerifiedClaims> opt = tokenVerifierPort.verifyAccess(raw, now);
        if (opt.isEmpty()) return Optional.empty();

        VerifiedClaims claims = opt.get();
        if (accessTokenBlacklistPort.isBlacklisted(claims.jti())) {
            return Optional.empty();
        }

        return loadAccountPort.findById(claims.accountId())
                .map(this::toAuthenticated);
    }

    private AuthenticatedAccount toAuthenticated(Account account) {
        return new AuthenticatedAccount(account.getId(), account.getEmail(), account.getTier());
    }
}
