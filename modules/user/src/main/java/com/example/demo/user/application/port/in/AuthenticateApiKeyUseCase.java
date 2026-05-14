package com.example.demo.user.application.port.in;

import java.time.Instant;
import java.util.Optional;

public interface AuthenticateApiKeyUseCase {
    Optional<AuthenticatedApiKey> authenticate(String authorizationHeader, String clientIp, Instant now);
}
