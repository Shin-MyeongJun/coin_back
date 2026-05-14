package com.example.demo.user.infrastructure.web.dto;

public record SignupResponse(String accountId, String email, String tier, long createdAt) {}
