package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.RefreshTokenStorePort;
import com.example.demo.user.application.port.out.TokenIssuerPort;
import com.example.demo.user.application.port.out.TokenVerifierPort;
import com.example.demo.user.application.port.out.VerifiedClaims;
import com.example.demo.user.domain.domain.AccessToken;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.domain.RefreshToken;
import com.example.demo.user.domain.domain.TokenPair;
import com.example.demo.user.domain.exception.TokenInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock TokenVerifierPort tokenVerifierPort;
    @Mock TokenIssuerPort tokenIssuerPort;
    @Mock RefreshTokenStorePort refreshTokenStorePort;
    @Mock LoadAccountPort loadAccountPort;

    @InjectMocks RefreshTokenService service;

    @Test
    void rejects_when_jwt_invalid() {
        given(tokenVerifierPort.verifyRefresh(any(), any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh("bad", Instant.now()))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void rejects_when_not_in_allowlist() {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        VerifiedClaims c = new VerifiedClaims(id, "old-jti", AccountTier.FREE, now.plusSeconds(60));
        given(tokenVerifierPort.verifyRefresh(any(), any())).willReturn(Optional.of(c));
        given(refreshTokenStorePort.isAllowed(id, "old-jti")).willReturn(false);

        assertThatThrownBy(() -> service.refresh("RT", now))
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    void rotates_revoking_old_and_allowing_new() {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        VerifiedClaims c = new VerifiedClaims(id, "old-jti", AccountTier.FREE, now.plusSeconds(60));
        Account a = new Account(id, Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                AccountTier.FREE, now, now);
        TokenPair newPair = new TokenPair(
                new AccessToken("AT", "new-access-jti", now.plusSeconds(300)),
                new RefreshToken("RT", "new-refresh-jti", id, now.plusSeconds(1209600))
        );

        given(tokenVerifierPort.verifyRefresh(any(), any())).willReturn(Optional.of(c));
        given(refreshTokenStorePort.isAllowed(id, "old-jti")).willReturn(true);
        given(loadAccountPort.findById(id)).willReturn(Optional.of(a));
        given(tokenIssuerPort.issue(id, AccountTier.FREE, now)).willReturn(newPair);

        TokenPair out = service.refresh("RT", now);

        assertThat(out).isSameAs(newPair);
        verify(refreshTokenStorePort).revoke(id, "old-jti");
        verify(refreshTokenStorePort).allow(id, "new-refresh-jti", now.plusSeconds(1209600));
    }
}
