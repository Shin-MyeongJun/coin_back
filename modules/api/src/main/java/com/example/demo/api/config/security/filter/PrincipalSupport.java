package com.example.demo.api.config.security.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Request attribute 기반 principal 저장/조회 유틸.
 * SecurityContextHolder 의존을 피하기 위해 별도로 둔다.
 */
public final class PrincipalSupport {

    private PrincipalSupport() {}

    public static void store(HttpServletRequest request, AuthenticationPrincipal principal) {
        request.setAttribute(AuthenticationPrincipal.REQUEST_ATTRIBUTE, principal);
    }

    public static Optional<AuthenticationPrincipal> current(HttpServletRequest request) {
        Object v = request.getAttribute(AuthenticationPrincipal.REQUEST_ATTRIBUTE);
        return v instanceof AuthenticationPrincipal p ? Optional.of(p) : Optional.empty();
    }

    public static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma >= 0 ? xff.substring(0, comma) : xff).trim();
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) {
            return real.trim();
        }
        String remote = request.getRemoteAddr();
        return (remote == null || remote.isBlank()) ? "unknown" : remote;
    }
}
