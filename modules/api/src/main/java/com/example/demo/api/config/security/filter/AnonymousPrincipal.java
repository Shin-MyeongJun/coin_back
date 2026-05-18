package com.example.demo.api.config.security.filter;

public record AnonymousPrincipal(String clientIp) implements AuthenticationPrincipal {

    public AnonymousPrincipal {
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }
}
