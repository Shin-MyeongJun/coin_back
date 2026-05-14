package com.example.demo.user.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IssueApiKeyRequest(
        @NotBlank @Size(max = 100) String label,
        @NotEmpty List<@NotBlank String> scopes,
        List<@NotBlank String> ipAllowlist
) {}
