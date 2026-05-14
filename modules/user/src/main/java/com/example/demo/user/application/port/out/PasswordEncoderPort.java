package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.PasswordHash;

public interface PasswordEncoderPort {
    PasswordHash encode(String rawPassword);
    boolean matches(String rawPassword, PasswordHash hash);
}
