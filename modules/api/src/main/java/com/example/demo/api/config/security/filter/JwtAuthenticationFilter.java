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
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;

/**
 * Authorization: Bearer ... 만 처리.
 *
 * <p>이전 필터(SseTicketFilter)가 principal 을 이미 주입했다면 그대로 통과.
 * 헤더가 없거나 ApiKey scheme 이면 다음 필터로 위임(여기서 401 으로 끊지 않음).
 * Bearer 토큰이 있지만 검증 실패한 경우에만 즉시 401 RFC7807 응답.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

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
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        Optional<AuthenticatedAccount> result =
                verifyAccessTokenUseCase.verify(header, clock.instant());
        if (result.isEmpty()) {
            ProblemDetails.write(response, objectMapper, HttpStatus.UNAUTHORIZED,
                    "Authentication required",
                    "Invalid or expired access token",
                    "JWT_INVALID", -1L);
            return;
        }
        AuthenticatedAccount account = result.get();
        PrincipalSupport.store(request, new JwtPrincipal(account.id(), account.tier()));
        chain.doFilter(request, response);
    }
}
