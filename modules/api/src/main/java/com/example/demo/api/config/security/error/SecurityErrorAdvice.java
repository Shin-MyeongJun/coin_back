package com.example.demo.api.config.security.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * 컨트롤러 단계에서 발생한 권한/스코프 부족 예외를 RFC 7807 응답으로 변환.
 * 필터에서 발생하는 401/429는 {@link ProblemDetails} 가 직접 응답을 작성한다.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityErrorAdvice {

    private static final URI TYPE_BLANK = URI.create("about:blank");

    @ExceptionHandler(InsufficientScopeException.class)
    public ProblemDetail handleInsufficientScope(InsufficientScopeException ex,
                                                 HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setType(TYPE_BLANK);
        pd.setTitle("Insufficient scope");
        pd.setDetail(ex.getMessage());
        pd.setProperty("code", "INSUFFICIENT_SCOPE");
        pd.setProperty("requiredScopes", ex.required());
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }
}
