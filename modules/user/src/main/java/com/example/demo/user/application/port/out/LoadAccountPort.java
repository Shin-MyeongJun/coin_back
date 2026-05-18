package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.Email;

import java.util.Optional;

public interface LoadAccountPort {
    Optional<Account> findById(AccountId id);
    Optional<Account> findByIdForUpdate(AccountId id);
    Optional<Account> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
