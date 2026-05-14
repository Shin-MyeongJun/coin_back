package com.example.demo.user.application.port.out;

import java.time.Instant;
import java.util.Optional;

public interface TokenVerifierPort {
    Optional<VerifiedClaims> verifyAccess(String rawToken, Instant now);
    Optional<VerifiedClaims> verifyRefresh(String rawToken, Instant now);
}
