package com.example.demo.user.domain.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void of_normalizes_whitespace_and_case() {
        Email e = Email.of("  Alice@Example.COM  ");
        assertThat(e.value()).isEqualTo("alice@example.com");
    }

    @Test
    void of_rejects_invalid_format() {
        assertThatThrownBy(() -> Email.of("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_rejects_null() {
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(NullPointerException.class);
    }
}
