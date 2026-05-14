package com.example.demo.user.domain.exception;

public class TokenInvalidException extends RuntimeException {
    public TokenInvalidException(String reason) {
        super("Token invalid: " + reason);
    }
}
