package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.AuthenticatedApiKey;
import com.example.demo.user.application.port.out.ApiKeyLookupCachePort;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.application.port.out.LoadApiKeyByPrefixPort;
import com.example.demo.user.application.port.out.SaveApiKeyPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeySecret;
import com.example.demo.user.domain.domain.PrefixAndSecret;
import com.example.demo.user.domain.service.ApiKeyHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticateApiKeyService implements AuthenticateApiKeyUseCase {

    private static final String SCHEME = "ApiKey ";

    private final LoadApiKeyByPrefixPort loadApiKeyByPrefixPort;
    private final ApiKeyLookupCachePort apiKeyLookupCachePort;
    private final ApiKeyHasher apiKeyHasher;
    private final LoadAccountPort loadAccountPort;
    private final SaveApiKeyPort saveApiKeyPort;

    @Override
    public Optional<AuthenticatedApiKey> authenticate(String authorizationHeader, String clientIp, Instant now) {
        PrefixAndSecret parsed = parseHeader(authorizationHeader);
        if (parsed == null) return Optional.empty();

        Optional<ApiKey> cached = apiKeyLookupCachePort.get(parsed.prefix());
        ApiKey apiKey;
        if (cached.isPresent()) {
            apiKey = cached.get();
        } else {
            Optional<ApiKey> fromDb = loadApiKeyByPrefixPort.findByPrefix(parsed.prefix());
            if (fromDb.isEmpty()) return Optional.empty();
            apiKey = fromDb.get();
        }

        // Hash comparison runs even on cache hit — defends against prefix collisions / cache poisoning.
        if (!apiKeyHasher.matches(parsed.secret(), apiKey.getHash())) return Optional.empty();
        if (!apiKey.isActive(now)) return Optional.empty();
        if (!apiKey.isIpAllowed(clientIp)) return Optional.empty();

        Optional<Account> accountOpt = loadAccountPort.findById(apiKey.getAccountId());
        if (accountOpt.isEmpty()) return Optional.empty();

        apiKey.touchUsage(now);
        ApiKey saved = saveApiKeyPort.save(apiKey);
        apiKeyLookupCachePort.put(saved);

        return Optional.of(new AuthenticatedApiKey(
                saved.getId(),
                saved.getAccountId(),
                accountOpt.get().getTier(),
                saved.getScopes(),
                saved.getPolicy()
        ));
    }

    private PrefixAndSecret parseHeader(String header) {
        if (header == null || header.isBlank()) return null;
        String trimmed = header.trim();
        if (!trimmed.startsWith(SCHEME)) return null;
        String body = trimmed.substring(SCHEME.length()).trim();
        int dot = body.indexOf('.');
        if (dot < 0 || dot == body.length() - 1) return null;
        String prefixStr = body.substring(0, dot);
        String secretStr = body.substring(dot + 1);
        try {
            return new PrefixAndSecret(new ApiKeyPrefix(prefixStr), new ApiKeySecret(secretStr));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
