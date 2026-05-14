package com.example.demo.user.infrastructure.cache;

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.user.application.port.out.ApiKeyLookupCachePort;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisApiKeyLookupCacheAdapter implements ApiKeyLookupCachePort {

    static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${app.env:local}")
    private String env;

    @Override
    public Optional<ApiKey> get(ApiKeyPrefix prefix) {
        String key = RedisKeys.apiKeyLookup(env, prefix.value());
        String json = redis.opsForValue().get(key);
        if (json == null) return Optional.empty();
        try {
            CachedApiKey cached = objectMapper.readValue(json, CachedApiKey.class);
            return Optional.of(cached.toDomain());
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize cached ApiKey for prefix={}, evicting", prefix.value(), ex);
            redis.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void put(ApiKey apiKey) {
        String key = RedisKeys.apiKeyLookup(env, apiKey.getPrefix().value());
        try {
            String json = objectMapper.writeValueAsString(CachedApiKey.fromDomain(apiKey));
            redis.opsForValue().set(key, json, TTL);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize ApiKey for cache prefix={}", apiKey.getPrefix().value(), ex);
        }
    }

    @Override
    public void evict(ApiKeyPrefix prefix) {
        redis.delete(RedisKeys.apiKeyLookup(env, prefix.value()));
    }

    /** Internal JSON DTO for cache value — mirrors the persistent ApiKey snapshot. */
    record CachedApiKey(
            String id,
            String accountId,
            String label,
            String prefix,
            String hash,
            List<String> scopes,
            List<String> ipAllowlist,
            int rpm,
            int rpd,
            int sseConcurrent,
            Long createdAt,
            Long revokedAt,
            Long lastUsedAt
    ) {
        static CachedApiKey fromDomain(ApiKey k) {
            return new CachedApiKey(
                    k.getId().asString(),
                    k.getAccountId().asString(),
                    k.getLabel(),
                    k.getPrefix().value(),
                    k.getHash().value(),
                    k.getScopes().stream().map(Enum::name).toList(),
                    List.copyOf(k.getIpAllowlist()),
                    k.getPolicy().rpm(),
                    k.getPolicy().rpd(),
                    k.getPolicy().sseConcurrent(),
                    k.getCreatedAt() == null ? null : k.getCreatedAt().toEpochMilli(),
                    k.getRevokedAt() == null ? null : k.getRevokedAt().toEpochMilli(),
                    k.getLastUsedAt() == null ? null : k.getLastUsedAt().toEpochMilli()
            );
        }

        ApiKey toDomain() {
            Set<ApiKeyScope> scopeSet = scopes == null || scopes.isEmpty()
                    ? Collections.emptySet()
                    : scopes.stream().map(ApiKeyScope::valueOf)
                            .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(ApiKeyScope.class)));
            Set<String> ipSet = ipAllowlist == null || ipAllowlist.isEmpty()
                    ? Collections.emptySet()
                    : new LinkedHashSet<>(ipAllowlist);
            return ApiKey.restore(
                    ApiKeyId.of(UUID.fromString(id)),
                    AccountId.of(UUID.fromString(accountId)),
                    label,
                    ApiKeyPrefix.of(prefix),
                    ApiKeyHash.of(hash),
                    scopeSet,
                    ipSet,
                    new RateLimitPolicy(rpm, rpd, sseConcurrent),
                    createdAt == null ? null : Instant.ofEpochMilli(createdAt),
                    revokedAt == null ? null : Instant.ofEpochMilli(revokedAt),
                    lastUsedAt == null ? null : Instant.ofEpochMilli(lastUsedAt)
            );
        }
    }
}
