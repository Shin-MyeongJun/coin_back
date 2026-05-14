package com.example.demo.user.infrastructure.web.dto;

import java.util.List;

/**
 * List response — secret/hash never included. {@code prefix} is shown so the user can
 * identify which key is which.
 */
public record ApiKeySummaryResponse(
        String id,
        String label,
        String prefix,
        List<String> scopes,
        List<String> ipAllowlist,
        PolicyResponse policy,
        long createdAt,
        Long revokedAt,
        Long lastUsedAt
) {}
