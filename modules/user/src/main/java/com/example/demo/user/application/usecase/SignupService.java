package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.SignupUseCase;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.PasswordEncoderPort;
import com.example.demo.user.application.port.out.SaveAccountPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.PasswordHash;
import com.example.demo.user.domain.exception.DuplicateEmailException;
import com.example.demo.user.domain.service.AccountFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SignupService implements SignupUseCase {

    private final LoadAccountPort loadAccountPort;
    private final SaveAccountPort saveAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Override
    @Transactional
    public Account signup(Email email, String rawPassword, Instant now) {
        if (loadAccountPort.existsByEmail(email)) {
            throw new DuplicateEmailException(email.value());
        }
        PasswordHash hash = passwordEncoderPort.encode(rawPassword);
        Account account = AccountFactory.create(email, hash, now);
        try {
            return saveAccountPort.save(account);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(email.value());
        }
    }
}
