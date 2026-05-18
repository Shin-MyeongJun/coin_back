package com.example.demo.api.config.security.ssetoken;

import com.example.demo.api.config.security.filter.ApiKeyPrincipal;
import com.example.demo.api.config.security.filter.AuthenticationPrincipal;
import com.example.demo.api.config.security.filter.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class IssueSseTicketService implements IssueSseTicketUseCase {

    private static final int TOKEN_BYTES = 32;
    private static final int MAX_STORE_RETRIES = 3;
    private static final SecureRandom RNG = new SecureRandom();

    private final ConsumeSseTicketPort consumeSseTicketPort;

    @Override
    public Issued issue(AuthenticationPrincipal principal, Duration ttl, Instant now) {
        if (principal == null || !principal.isAuthenticated()) {
            throw new IllegalStateException("SSE ticket can only be issued for authenticated principals");
        }
        SseTicketPayload payload = toPayload(principal);

        for (int i = 0; i < MAX_STORE_RETRIES; i++) {
            String token = generateToken();
            if (consumeSseTicketPort.store(token, payload, ttl)) {
                return new Issued(token, now.plus(ttl));
            }
        }
        throw new IllegalStateException("Failed to allocate SSE ticket after " + MAX_STORE_RETRIES + " attempts");
    }

    private SseTicketPayload toPayload(AuthenticationPrincipal principal) {
        return switch (principal) {
            case JwtPrincipal j -> SseTicketPayload.jwt(j.accountId(), j.tier());
            case ApiKeyPrincipal a -> SseTicketPayload.apiKey(
                    a.apiKeyId(), a.accountId(), a.tier(), a.scopes(), a.policy());
            case com.example.demo.api.config.security.filter.AnonymousPrincipal ignored ->
                    throw new IllegalStateException("Anonymous principals cannot issue SSE tickets");
        };
    }

    private String generateToken() {
        byte[] buf = new byte[TOKEN_BYTES];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
