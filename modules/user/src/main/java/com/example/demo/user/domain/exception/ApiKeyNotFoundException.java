package com.example.demo.user.domain.exception;

import com.example.demo.user.domain.domain.ApiKeyId;

public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException(ApiKeyId id) {
        super("ApiKey not found: " + id.asString());
    }
}
