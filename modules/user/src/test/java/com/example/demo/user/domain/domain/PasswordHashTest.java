package com.example.demo.user.domain.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordHashTest {

    @Test
    void of_accepts_bcrypt_hash() {
        PasswordHash h = PasswordHash.of("$2a$12$abcdefghijklmnopqrstuv");
        assertThat(h.value()).startsWith("$2");
    }

    @Test
    void of_rejects_non_bcrypt_string() {
        assertThatThrownBy(() -> PasswordHash.of("plaintext"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
