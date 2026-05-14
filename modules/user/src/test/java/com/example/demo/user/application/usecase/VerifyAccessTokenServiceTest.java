package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.out.AccessTokenBlacklistPort;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.TokenVerifierPort;
import com.example.demo.user.application.port.out.VerifiedClaims;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VerifyAccessTokenServiceTest {

    @Mock TokenVerifierPort tokenVerifierPort;
    @Mock AccessTokenBlacklistPort accessTokenBlacklistPort;
    @Mock LoadAccountPort loadAccountPort;

    @InjectMocks VerifyAccessTokenService service;

    @Test
    void returns_empty_when_token_blank() {
        assertThat(service.verify("", Instant.now())).isEmpty();
        assertThat(service.verify(null, Instant.now())).isEmpty();
    }

    @Test
    void returns_empty_when_jwt_invalid() {
        given(tokenVerifierPort.verifyAccess(any(), any())).willReturn(Optional.empty());
        assertThat(service.verify("Bearer x", Instant.now())).isEmpty();
    }

    @Test
    void returns_empty_when_blacklisted() {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        VerifiedClaims c = new VerifiedClaims(id, "jti", AccountTier.FREE, now.plusSeconds(60));
        given(tokenVerifierPort.verifyAccess(any(), any())).willReturn(Optional.of(c));
        given(accessTokenBlacklistPort.isBlacklisted("jti")).willReturn(true);

        assertThat(service.verify("Bearer x", now)).isEmpty();
    }

    @Test
    void returns_authenticated_account_when_ok() {
        AccountId id = AccountId.generate();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        VerifiedClaims c = new VerifiedClaims(id, "jti", AccountTier.PRO, now.plusSeconds(60));
        Account a = new Account(id, Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                AccountTier.PRO, now, now);

        given(tokenVerifierPort.verifyAccess(any(), any())).willReturn(Optional.of(c));
        given(accessTokenBlacklistPort.isBlacklisted("jti")).willReturn(false);
        given(loadAccountPort.findById(id)).willReturn(Optional.of(a));

        Optional<AuthenticatedAccount> result = service.verify("Bearer x", now);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(id);
        assertThat(result.get().email()).isEqualTo(a.getEmail());
        assertThat(result.get().tier()).isEqualTo(AccountTier.PRO);
    }
}
