package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.PasswordEncoderPort;
import com.example.demo.user.application.port.out.SaveAccountPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.exception.DuplicateEmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock LoadAccountPort loadAccountPort;
    @Mock SaveAccountPort saveAccountPort;
    @Mock PasswordEncoderPort passwordEncoderPort;

    @InjectMocks SignupService service;

    @Test
    void rejects_when_email_exists() {
        Email e = Email.of("a@b.com");
        given(loadAccountPort.existsByEmail(e)).willReturn(true);

        assertThatThrownBy(() -> service.signup(e, "password1234", Instant.now()))
                .isInstanceOf(DuplicateEmailException.class);
        verify(saveAccountPort, never()).save(any());
    }

    @Test
    void encodes_and_saves_when_new() {
        Email e = Email.of("a@b.com");
        PasswordHash h = PasswordHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA");
        given(loadAccountPort.existsByEmail(e)).willReturn(false);
        given(passwordEncoderPort.encode("password1234")).willReturn(h);
        given(saveAccountPort.save(any())).willAnswer(inv -> inv.getArgument(0));

        Account saved = service.signup(e, "password1234", Instant.parse("2026-05-14T00:00:00Z"));

        assertThat(saved.getEmail()).isEqualTo(e);
        assertThat(saved.getPasswordHash()).isEqualTo(h);
        verify(passwordEncoderPort).encode("password1234");
        verify(saveAccountPort).save(any());
    }
}
