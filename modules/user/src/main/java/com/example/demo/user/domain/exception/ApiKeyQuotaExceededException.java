package com.example.demo.user.domain.exception;

import com.example.demo.user.domain.domain.AccountTier;

public class ApiKeyQuotaExceededException extends RuntimeException {
    public ApiKeyQuotaExceededException(AccountTier tier, int max, long currentActive) {
        super("API key quota exceeded for tier " + tier + ": max=" + max + ", active=" + currentActive);
    }
}
