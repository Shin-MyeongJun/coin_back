package com.example.demo.api.config;

import com.example.demo.api.config.security.filter.JwtPrincipal;
import com.example.demo.api.config.security.filter.PrincipalSupport;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

class JwtPrincipalAuthorization implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
                                       RequestAuthorizationContext context) {
        boolean granted = PrincipalSupport.current(context.getRequest())
                .filter(JwtPrincipal.class::isInstance)
                .map(JwtPrincipal.class::cast)
                .map(JwtPrincipal::isAuthenticated)
                .orElse(false);
        return new AuthorizationDecision(granted);
    }
}
