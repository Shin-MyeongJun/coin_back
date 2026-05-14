package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.Email;

import java.time.Instant;

public interface SignupUseCase {
    Account signup(Email email, String rawPassword, Instant now);
}
