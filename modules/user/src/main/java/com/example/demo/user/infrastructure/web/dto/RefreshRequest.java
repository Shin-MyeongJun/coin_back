package com.example.demo.user.infrastructure.web.dto;

/**
 * Reserved for clients that cannot use cookies (e.g., native apps).
 * The HTTP /refresh endpoint primarily reads the refresh token from the httpOnly cookie.
 */
public record RefreshRequest(String refreshToken) {}
