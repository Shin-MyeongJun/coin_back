package com.example.demo.api.config.security.filter;

/**
 * api 모듈의 인증 주체 sealed 계층.
 * SecurityContextHolder 대신 {@link jakarta.servlet.http.HttpServletRequest} 의
 * attribute({@link #REQUEST_ATTRIBUTE})로 전파한다.
 */
public sealed interface AuthenticationPrincipal
        permits JwtPrincipal, ApiKeyPrincipal, AnonymousPrincipal {

    /**
     * 요청 attribute key. 모든 필터/컨트롤러에서 공통 사용한다.
     */
    String REQUEST_ATTRIBUTE = "ys.principal";

    boolean isAuthenticated();
}
