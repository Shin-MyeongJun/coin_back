package com.example.demo.user.domain.exception;

import com.example.demo.user.domain.domain.AccountId;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId id) {
        super("Account not found: " + id.asString());
    }
}
