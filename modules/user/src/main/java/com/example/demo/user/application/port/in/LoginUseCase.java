package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.TokenPair;

import java.time.Instant;

public interface LoginUseCase {
    TokenPair login(Email email, String rawPassword, Instant now);
}
