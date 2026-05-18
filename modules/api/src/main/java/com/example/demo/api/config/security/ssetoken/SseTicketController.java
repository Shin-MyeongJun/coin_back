package com.example.demo.api.config.security.ssetoken;

import com.example.demo.api.config.security.filter.AnonymousPrincipal;
import com.example.demo.api.config.security.filter.AuthenticationPrincipal;
import com.example.demo.api.config.security.filter.PrincipalSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SseTicketController {

    static final Duration TICKET_TTL = Duration.ofSeconds(60);

    private final IssueSseTicketUseCase issueSseTicketUseCase;
    private final Clock clock;

    @PostMapping("/sse-ticket")
    public ResponseEntity<SseTicketResponse> issue(HttpServletRequest request) {
        AuthenticationPrincipal principal = PrincipalSupport.current(request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authentication required"));
        if (principal instanceof AnonymousPrincipal) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        IssueSseTicketUseCase.Issued issued =
                issueSseTicketUseCase.issue(principal, TICKET_TTL, clock.instant());
        return ResponseEntity.ok(new SseTicketResponse(
                issued.ticket(), issued.expiresAt().toEpochMilli()));
    }

    public record SseTicketResponse(String ticket, long expiresAt) {}
}
