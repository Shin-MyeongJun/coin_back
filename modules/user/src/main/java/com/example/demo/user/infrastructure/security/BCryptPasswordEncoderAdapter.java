package com.example.demo.user.infrastructure.security;

import com.example.demo.user.application.port.out.PasswordEncoderPort;
import com.example.demo.user.domain.domain.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int STRENGTH = 12;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);

    @Override
    public PasswordHash encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }
        return PasswordHash.of(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash hash) {
        if (rawPassword == null || hash == null) return false;
        return encoder.matches(rawPassword, hash.value());
    }
}
