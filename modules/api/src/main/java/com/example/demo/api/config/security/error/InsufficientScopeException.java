package com.example.demo.api.config.security.error;

import com.example.demo.user.domain.domain.ApiKeyScope;

import java.util.Set;

/**
 * ApiKeyPrincipal 의 scope 가 부족할 때 컨트롤러에서 throw.
 * SecurityErrorAdvice 가 403 RFC7807 응답으로 변환한다.
 */
public class InsufficientScopeException extends RuntimeException {

    private final Set<ApiKeyScope> required;

    public InsufficientScopeException(Set<ApiKeyScope> required) {
        super("Insufficient scope: required " + required);
        this.required = Set.copyOf(required);
    }

    public Set<ApiKeyScope> required() {
        return required;
    }
}
