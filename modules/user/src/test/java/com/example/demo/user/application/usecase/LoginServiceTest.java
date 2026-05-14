package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.PasswordEncoderPort;
import com.example.demo.user.application.port.out.RefreshTokenStorePort;
import com.example.demo.user.application.port.out.TokenIssuerPort;
import com.example.demo.user.domain.domain.AccessToken;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.domain.RefreshToken;
import com.example.demo.user.domain.domain.TokenPair;
import com.example.demo.user.domain.exception.InvalidCredentialsException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock LoadAccountPort loadAccountPort;
    @Mock PasswordEncoderPort passwordEncoderPort;
    @Mock TokenIssuerPort tokenIssuerPort;
    @Mock RefreshTokenStorePort refreshTokenStorePort;

    @InjectMocks LoginService service;

    private Account stubAccount() {
        Instant t0 = Instant.parse("2026-05-14T00:00:00Z");
        return new Account(
                AccountId.generate(),
                Email.of("a@b.com"),
                PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                AccountTier.FREE,
                t0,
                t0
        );
    }

    @Test
    void rejects_wrong_password() {
        Account a = stubAccount();
        given(loadAccountPort.findByEmail(any())).willReturn(Optional.of(a));
        given(passwordEncoderPort.matches(eq("wrong"), any())).willReturn(false);

        assertThatThrownBy(() -> service.login(a.getEmail(), "wrong", Instant.now()))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejects_unknown_email() {
        given(loadAccountPort.findByEmail(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(Email.of("nope@x.com"), "pw", Instant.now()))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void issues_tokens_and_allows_refresh_jti_when_ok() {
        Account a = stubAccount();
        Instant now = Instant.parse("2026-05-14T00:00:00Z");
        Instant accessExp = now.plusSeconds(300);
        Instant refreshExp = now.plusSeconds(1209600);
        TokenPair pair = new TokenPair(
                new AccessToken("AT", "access-jti", accessExp),
                new RefreshToken("RT", "refresh-jti", a.getId(), refreshExp)
        );

        given(loadAccountPort.findByEmail(a.getEmail())).willReturn(Optional.of(a));
        given(passwordEncoderPort.matches(anyString(), any())).willReturn(true);
        given(tokenIssuerPort.issue(a.getId(), a.getTier(), now)).willReturn(pair);

        TokenPair out = service.login(a.getEmail(), "pw", now);

        assertThat(out).isSameAs(pair);
        verify(refreshTokenStorePort).allow(a.getId(), "refresh-jti", refreshExp);
    }
}
