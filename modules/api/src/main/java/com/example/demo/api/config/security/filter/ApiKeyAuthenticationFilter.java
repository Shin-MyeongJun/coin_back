package com.example.demo.api.config.security.filter;

import com.example.demo.api.config.security.error.ProblemDetails;
import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.AuthenticatedApiKey;
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
 * Authorization: ApiKey ... 만 처리.
 *
 * <p>이전 필터에서 principal 이 이미 주입되었다면 그대로 통과.
 * 헤더가 없거나 Bearer scheme 이면 다음 필터로 위임.
 * ApiKey scheme 인데 인증 실패하면 401 RFC7807 즉시 응답.
 */
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_PREFIX = "ApiKey ";

    private final AuthenticateApiKeyUseCase authenticateApiKeyUseCase;
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
        if (header == null || !header.startsWith(API_KEY_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        String clientIp = PrincipalSupport.clientIp(request);
        Optional<AuthenticatedApiKey> result =
                authenticateApiKeyUseCase.authenticate(header, clientIp, clock.instant());
        if (result.isEmpty()) {
            ProblemDetails.write(response, objectMapper, HttpStatus.UNAUTHORIZED,
                    "Authentication required",
                    "Invalid API key, revoked, or IP not allowed",
                    "API_KEY_INVALID", -1L);
            return;
        }
        AuthenticatedApiKey k = result.get();
        PrincipalSupport.store(request, new ApiKeyPrincipal(
                k.apiKeyId(), k.accountId(), k.tier(), k.scopes(), k.policy()));
        chain.doFilter(request, response);
    }
}
