package com.example.demo.user.infrastructure.web.dto;

import java.util.List;

/**
 * Returned exactly once at issuance time. {@code secret} is the plaintext API key secret —
 * never returned again, never persisted in plaintext.
 */
public record IssueApiKeyResponse(
        String id,
        String label,
        String prefix,
        String secret,
        List<String> scopes,
        List<String> ipAllowlist,
        PolicyResponse policy,
        long createdAt
) {}
