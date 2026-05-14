package com.example.demo.user.infrastructure.security;

import com.example.demo.user.application.port.out.TokenIssuerPort;
import com.example.demo.user.domain.domain.AccessToken;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.RefreshToken;
import com.example.demo.user.domain.domain.TokenPair;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    static final String CLAIM_TIER = "tier";
    static final String CLAIM_TYPE = "type";
    static final String TYPE_ACCESS = "access";
    static final String TYPE_REFRESH = "refresh";

    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public TokenPair issue(AccountId accountId, AccountTier tier, Instant now) {
        Instant accessExp = now.plus(Duration.ofSeconds(props.accessTtlSeconds()));
        Instant refreshExp = now.plus(Duration.ofSeconds(props.refreshTtlSeconds()));
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessRaw = Jwts.builder()
                .issuer(props.issuer())
                .subject(accountId.asString())
                .id(accessJti)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_TIER, tier.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExp))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();

        String refreshRaw = Jwts.builder()
                .issuer(props.issuer())
                .subject(accountId.asString())
                .id(refreshJti)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_TIER, tier.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExp))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();

        return new TokenPair(
                new AccessToken(accessRaw, accessJti, accessExp),
                new RefreshToken(refreshRaw, refreshJti, accountId, refreshExp)
        );
    }
}
