package com.example.demo.user.domain.exception;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKeyId;

public class ApiKeyOwnershipException extends RuntimeException {
    public ApiKeyOwnershipException(AccountId callerAccountId, ApiKeyId apiKeyId) {
        super("Account " + callerAccountId.asString()
                + " does not own ApiKey " + apiKeyId.asString());
    }
}
