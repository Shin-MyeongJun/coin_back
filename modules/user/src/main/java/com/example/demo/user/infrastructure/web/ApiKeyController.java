package com.example.demo.user.infrastructure.web;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.IssueApiKeyUseCase;
import com.example.demo.user.application.port.in.IssuedApiKey;
import com.example.demo.user.application.port.in.ListApiKeysQuery;
import com.example.demo.user.application.port.in.RevokeApiKeyUseCase;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.exception.TokenInvalidException;
import com.example.demo.user.infrastructure.web.dto.ApiKeySummaryResponse;
import com.example.demo.user.infrastructure.web.dto.IssueApiKeyRequest;
import com.example.demo.user.infrastructure.web.dto.IssueApiKeyResponse;
import com.example.demo.user.infrastructure.web.dto.PolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final VerifyAccessTokenUseCase verifyAccessTokenUseCase;
    private final IssueApiKeyUseCase issueApiKeyUseCase;
    private final RevokeApiKeyUseCase revokeApiKeyUseCase;
    private final ListApiKeysQuery listApiKeysQuery;

    @PostMapping
    public ResponseEntity<IssueApiKeyResponse> issue(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody IssueApiKeyRequest req
    ) {
        AccountId accountId = resolveAccount(authHeader);
        Set<ApiKeyScope> scopes = parseScopes(req.scopes());
        Set<String> ipAllowlist = req.ipAllowlist() == null
                ? Collections.emptySet()
                : new LinkedHashSet<>(req.ipAllowlist());

        IssuedApiKey issued = issueApiKeyUseCase.issue(
                accountId, req.label(), scopes, ipAllowlist, Instant.now()
        );

        ApiKey k = issued.apiKey();
        IssueApiKeyResponse body = new IssueApiKeyResponse(
                k.getId().asString(),
                k.getLabel(),
                k.getPrefix().value(),
                issued.secret().value(),
                k.getScopes().stream().map(Enum::name).toList(),
                List.copyOf(k.getIpAllowlist()),
                new PolicyResponse(k.getPolicy().rpm(), k.getPolicy().rpd(), k.getPolicy().sseConcurrent()),
                k.getCreatedAt().toEpochMilli()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public List<ApiKeySummaryResponse> list(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        AccountId accountId = resolveAccount(authHeader);
        return listApiKeysQuery.list(accountId).stream().map(this::toSummary).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") UUID id
    ) {
        AccountId accountId = resolveAccount(authHeader);
        revokeApiKeyUseCase.revoke(accountId, ApiKeyId.of(id), Instant.now());
        return ResponseEntity.noContent().build();
    }

    // TODO Step 4: replace with @AuthenticationPrincipal AuthenticatedAccount once SecurityFilter is in place.
    private AccountId resolveAccount(String authHeader) {
        return verifyAccessTokenUseCase.verify(authHeader, Instant.now())
                .map(AuthenticatedAccount::id)
                .orElseThrow(() -> new TokenInvalidException("missing or invalid access token"));
    }

    private Set<ApiKeyScope> parseScopes(List<String> raw) {
        Set<ApiKeyScope> out = EnumSet.noneOf(ApiKeyScope.class);
        for (String s : raw) {
            try {
                out.add(ApiKeyScope.valueOf(s));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown ApiKeyScope: " + s);
            }
        }
        return out;
    }

    private ApiKeySummaryResponse toSummary(ApiKey k) {
        return new ApiKeySummaryResponse(
                k.getId().asString(),
                k.getLabel(),
                k.getPrefix().value(),
                k.getScopes().stream().map(Enum::name).toList(),
                List.copyOf(k.getIpAllowlist()),
                new PolicyResponse(k.getPolicy().rpm(), k.getPolicy().rpd(), k.getPolicy().sseConcurrent()),
                k.getCreatedAt().toEpochMilli(),
                k.getRevokedAt() == null ? null : k.getRevokedAt().toEpochMilli(),
                k.getLastUsedAt() == null ? null : k.getLastUsedAt().toEpochMilli()
        );
    }
}
