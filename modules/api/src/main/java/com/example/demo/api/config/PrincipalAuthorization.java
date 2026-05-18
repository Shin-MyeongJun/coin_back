package com.example.demo.api.config;

import com.example.demo.api.config.security.filter.AuthenticationPrincipal;
import com.example.demo.api.config.security.filter.PrincipalSupport;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * SecurityContextHolder 가 아닌 request attribute({@code ys.principal}) 의 존재로 인가를 결정하는
 * 커스텀 {@link AuthorizationManager}.
 *
 * <p>{@link AuthenticationPrincipal#isAuthenticated()} 가 true 면 허용.
 */
class PrincipalAuthorization implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication,
                                       RequestAuthorizationContext context) {
        boolean granted = PrincipalSupport.current(context.getRequest())
                .map(AuthenticationPrincipal::isAuthenticated)
                .orElse(false);
        return new AuthorizationDecision(granted);
    }
}
