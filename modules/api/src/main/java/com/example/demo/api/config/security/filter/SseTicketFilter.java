package com.example.demo.api.config.security.filter;

import com.example.demo.api.config.security.error.ProblemDetails;
import com.example.demo.api.config.security.ssetoken.ConsumeSseTicketPort;
import com.example.demo.api.config.security.ssetoken.SseTicketPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * SSE 전용 URL 쿼리 ticket 인증.
 *
 * <p>경로가 {@code /api/v1/stream/**} 이고 쿼리에 {@code t=...} 가 있을 때만 동작한다.
 * Redis GETDEL 로 1회 소비하고 principal 을 request attribute 에 주입한다.
 * ticket 이 없거나 유효하지 않으면 401 RFC 7807 응답을 즉시 작성한다(다음 필터로 넘기지 않음).
 *
 * <p>다른 경로 또는 stream 경로지만 {@code t} 쿼리가 없으면 이 필터를 그대로 통과시킨다
 * (그 경우 Jwt/ApiKey 필터가 처리할 수 있음).
 */
@RequiredArgsConstructor
public class SseTicketFilter extends OncePerRequestFilter {

    private static final String STREAM_PATH_PATTERN = "/api/v1/stream/**";
    private static final String TICKET_PARAM = "t";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final ConsumeSseTicketPort consumeSseTicketPort;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!MATCHER.match(STREAM_PATH_PATTERN, path)) {
            chain.doFilter(request, response);
            return;
        }
        String ticket = request.getParameter(TICKET_PARAM);
        if (ticket == null || ticket.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        Optional<SseTicketPayload> consumed = consumeSseTicketPort.consume(ticket);
        if (consumed.isEmpty()) {
            ProblemDetails.write(response, objectMapper, HttpStatus.UNAUTHORIZED,
                    "Authentication required",
                    "Invalid or already-used SSE ticket",
                    "SSE_TICKET_INVALID", -1L);
            return;
        }

        SseTicketPayload payload = consumed.get();
        AuthenticationPrincipal principal = switch (payload.type()) {
            case JWT -> new JwtPrincipal(payload.accountId(), payload.tier());
            case API_KEY -> new ApiKeyPrincipal(
                    payload.apiKeyId(),
                    payload.accountId(),
                    payload.tier(),
                    payload.scopes(),
                    payload.policy());
        };
        PrincipalSupport.store(request, principal);
        chain.doFilter(request, response);
    }
}
