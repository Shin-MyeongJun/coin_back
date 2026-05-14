package com.example.demo.user.infrastructure.security;

import com.example.demo.user.application.port.out.TokenVerifierPort;
import com.example.demo.user.application.port.out.VerifiedClaims;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenVerifierAdapter implements TokenVerifierPort {

    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<VerifiedClaims> verifyAccess(String rawToken, Instant now) {
        return verify(rawToken, now, JwtTokenIssuerAdapter.TYPE_ACCESS);
    }

    @Override
    public Optional<VerifiedClaims> verifyRefresh(String rawToken, Instant now) {
        return verify(rawToken, now, JwtTokenIssuerAdapter.TYPE_REFRESH);
    }

    private Optional<VerifiedClaims> verify(String rawToken, Instant now, String expectedType) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        try {
            // Use the caller-supplied `now` as jjwt's clock so exp/nbf are evaluated against the
            // logical authentication time, not System.currentTimeMillis().
            io.jsonwebtoken.Clock clock = () -> java.util.Date.from(now);
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key())
                    .requireIssuer(props.issuer())
                    .clock(clock)
                    .build()
                    .parseSignedClaims(rawToken);

            Claims c = jws.getPayload();
            String type = c.get(JwtTokenIssuerAdapter.CLAIM_TYPE, String.class);
            if (!expectedType.equals(type)) {
                return Optional.empty();
            }
            Instant exp = c.getExpiration().toInstant();
            if (!exp.isAfter(now)) {
                return Optional.empty();
            }
            String tierName = c.get(JwtTokenIssuerAdapter.CLAIM_TIER, String.class);
            AccountTier tier = AccountTier.valueOf(tierName);
            AccountId id = AccountId.of(c.getSubject());
            return Optional.of(new VerifiedClaims(id, c.getId(), tier, exp));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
