package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.LoginUseCase;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.PasswordEncoderPort;
import com.example.demo.user.application.port.out.RefreshTokenStorePort;
import com.example.demo.user.application.port.out.TokenIssuerPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.TokenPair;
import com.example.demo.user.domain.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final LoadAccountPort loadAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenIssuerPort tokenIssuerPort;
    private final RefreshTokenStorePort refreshTokenStorePort;

    @Override
    public TokenPair login(Email email, String rawPassword, Instant now) {
        Account account = loadAccountPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoderPort.matches(rawPassword, account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        TokenPair pair = tokenIssuerPort.issue(account.getId(), account.getTier(), now);
        refreshTokenStorePort.allow(
                pair.refresh().accountId(),
                pair.refresh().jti(),
                pair.refresh().expiresAt()
        );
        return pair;
    }
}
