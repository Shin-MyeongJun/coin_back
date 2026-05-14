package com.example.demo.user.application.port.in;

import java.time.Instant;
import java.util.Optional;

public interface VerifyAccessTokenUseCase {
    Optional<AuthenticatedAccount> verify(String bearerToken, Instant now);
}
