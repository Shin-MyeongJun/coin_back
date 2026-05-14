package com.example.demo.user.domain.exception;

import com.example.demo.user.domain.domain.AccountTier;
import com.example.demo.user.domain.domain.ApiKeyScope;

public class ApiKeyScopeNotAllowedException extends RuntimeException {
    public ApiKeyScopeNotAllowedException(AccountTier tier, ApiKeyScope scope) {
        super("Scope " + scope + " not allowed for tier " + tier);
    }
}
