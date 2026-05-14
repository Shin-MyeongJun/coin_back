package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.TokenPair;

import java.time.Instant;

public interface RefreshTokenUseCase {
    TokenPair refresh(String refreshTokenRaw, Instant now);
}
