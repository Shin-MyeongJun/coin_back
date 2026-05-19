package com.example.demo.api.config.security.filter;

import com.example.demo.api.config.security.error.ProblemDetails;
import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Authorization: Bearer ... 처리.
 *
 * <p>SSE 화이트리스트 경로({@link #SSE_FALLBACK_PATHS})에 한해
 * 헤더가 없을 때 {@code ?access_token=} 쿼리 파라미터 fallback 을 허용한다.
 * EventSource 가 커스텀 헤더를 보낼 수 없는 한계를 보완하기 위함이다.
 *
 * <p>이전 필터(SseTicketFilter)가 principal 을 이미 주입했다면 그대로 통과.
 * 헤더/쿼리가 모두 없거나 ApiKey scheme 이면 다음 필터로 위임(여기서 401 으로 끊지 않음).
 * Bearer 토큰이 있지만 검증 실패한 경우에만 즉시 401 RFC7807 응답.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_PARAM = "access_token";

    /** GET 요청에 한해 ?access_token= fallback 을 허용하는 SSE 경로 패턴. */
    private static final List<String> SSE_FALLBACK_PATHS = List.of(
            "/api/v1/stream/**",
            "/api/v1/alert/stream/**"
    );

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final VerifyAccessTokenUseCase verifyAccessTokenUseCase;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (PrincipalSupport.current(request).isPresent()) {
            chain.doFilter(request, response);
            return;
        }
        String bearer = resolveBearerToken(request);
        if (bearer == null) {
            chain.doFilter(request, response);
            return;
        }
        Optional<AuthenticatedAccount> result =
                verifyAccessTokenUseCase.verify(bearer, clock.instant());
        if (result.isEmpty()) {
            ProblemDetails.write(response, objectMapper, HttpStatus.UNAUTHORIZED,
                    "Authentication required",
                    "Invalid or expired access token",
                    "JWT_INVALID", -1L);
            return;
        }
        AuthenticatedAccount account = result.get();
        PrincipalSupport.store(request, new JwtPrincipal(account.id(), account.tier()));
        String rawToken = bearer.substring(BEARER_PREFIX.length()).trim();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(account, rawToken, List.of())
        );
        chain.doFilter(request, response);
    }

    /**
     * Authorization 헤더 우선 → 화이트리스트 SSE GET 경로에 한해 ?access_token= fallback.
     * @return {@code "Bearer xxx"} 형태의 정규화된 헤더 값, 또는 미존재 시 {@code null}.
     */
    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header;
        }
        if (header != null) {
            // Authorization 헤더는 있지만 Bearer 가 아니다 (ex: ApiKey). 다음 필터로 위임.
            return null;
        }
        if (!isSseFallbackEligible(request)) {
            return null;
        }
        String token = request.getParameter(ACCESS_TOKEN_PARAM);
        if (token == null || token.isBlank()) {
            return null;
        }
        return BEARER_PREFIX + token;
    }

    private boolean isSseFallbackEligible(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        for (String pattern : SSE_FALLBACK_PATHS) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
