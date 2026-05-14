package com.example.demo.user.application.port.in;

import java.time.Instant;

public interface LogoutUseCase {
    void logout(String accessTokenRaw, String refreshTokenRaw, Instant now);
}
