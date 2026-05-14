package com.example.demo.user.infrastructure.cache;

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.user.application.port.out.AccessTokenBlacklistPort;
import com.example.demo.user.application.port.out.RefreshTokenStorePort;
import com.example.demo.user.domain.domain.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStoreAdapter implements RefreshTokenStorePort, AccessTokenBlacklistPort {

    static final String VALUE_PRESENT = "1";

    private final StringRedisTemplate redis;

    @Value("${app.env:local}")
    private String env;

    @Override
    public void allow(AccountId accountId, String jti, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) return;
        redis.opsForValue().set(
                RedisKeys.authRefresh(env, accountId.asString(), jti),
                VALUE_PRESENT,
                ttl
        );
    }

    @Override
    public boolean isAllowed(AccountId accountId, String jti) {
        Boolean exists = redis.hasKey(RedisKeys.authRefresh(env, accountId.asString(), jti));
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void revoke(AccountId accountId, String jti) {
        redis.delete(RedisKeys.authRefresh(env, accountId.asString(), jti));
    }

    @Override
    public void revokeAll(AccountId accountId) {
        // Pattern derived by passing wildcard as jti (RedisKeys validates non-blank, "*" passes).
        String pattern = RedisKeys.authRefresh(env, accountId.asString(), "*");
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(256).build();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                redis.delete(cursor.next());
            }
        }
    }

    @Override
    public void blacklist(String jti, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) return;
        redis.opsForValue().set(
                RedisKeys.authAccessBlacklist(env, jti),
                VALUE_PRESENT,
                ttl
        );
    }

    @Override
    public boolean isBlacklisted(String jti) {
        Boolean exists = redis.hasKey(RedisKeys.authAccessBlacklist(env, jti));
        return Boolean.TRUE.equals(exists);
    }
}
