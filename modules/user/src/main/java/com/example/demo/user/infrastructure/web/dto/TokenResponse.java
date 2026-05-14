package com.example.demo.user.infrastructure.web.dto;

public record TokenResponse(String accessToken, long expiresAt) {}
