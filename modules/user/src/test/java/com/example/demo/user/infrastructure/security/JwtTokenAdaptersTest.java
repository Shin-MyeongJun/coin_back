package com.example.demo.user.infrastructure.security;

import com.example.demo.user.application.port.out.VerifiedClaims;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenAdaptersTest {

    private static final String SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123"; // 64 bytes >= HS256 min

    private JwtTokenIssuerAdapter issuer;
    private JwtTokenVerifierAdapter verifier;
    private JwtProperties props;

    @BeforeEach
    void setup() {
        props = new JwtProperties(SECRET, 300, 1209600, "ys-coin");
        issuer = new JwtTokenIssuerAdapter(props);
        verifier = new JwtTokenVerifierAdapter(props);
    }

    @Test
    void issue_and_verify_access() {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        TokenPair pair = issuer.issue(id, AccountTier.FREE, now);

        Optional<VerifiedClaims> claims = verifier.verifyAccess(pair.access().raw(), now);

        assertThat(claims).isPresent();
        assertThat(claims.get().accountId()).isEqualTo(id);
        assertThat(claims.get().jti()).isEqualTo(pair.access().jti());
        assertThat(claims.get().tier()).isEqualTo(AccountTier.FREE);
    }

    @Test
    void verify_rejects_expired_token() {
        AccountId id = AccountId.generate();
        Instant issuedAt = Instant.parse("2026-05-14T00:00:00Z");
        TokenPair pair = issuer.issue(id, AccountTier.FREE, issuedAt);

        Instant afterAccessExp = issuedAt.plusSeconds(props.accessTtlSeconds() + 1);
        assertThat(verifier.verifyAccess(pair.access().raw(), afterAccessExp)).isEmpty();
    }

    @Test
    void access_token_is_not_verifiable_as_refresh() {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        TokenPair pair = issuer.issue(id, AccountTier.FREE, now);

        assertThat(verifier.verifyRefresh(pair.access().raw(), now)).isEmpty();
        assertThat(verifier.verifyAccess(pair.refresh().raw(), now)).isEmpty();
    }

    @Test
    void garbage_token_returns_empty() {
        assertThat(verifier.verifyAccess("not.a.jwt", Instant.now())).isEmpty();
    }
}
