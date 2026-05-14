package com.example.demo.user.infrastructure.persistence.mapper;

import com.example.demo.infra_shard.persistence.EntityMapping;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicy;
import com.example.demo.user.infrastructure.persistence.entity.ApiKeyEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class ApiKeyMapper implements EntityMapping<ApiKey, ApiKeyEntity> {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Override
    public ApiKeyEntity toEntity(ApiKey apiKey) {
        return ApiKeyEntity.builder()
                .id(apiKey.getId().value())
                .accountId(apiKey.getAccountId().value())
                .label(apiKey.getLabel())
                .prefix(apiKey.getPrefix().value())
                .hash(apiKey.getHash().value())
                .scopes(scopesToArray(apiKey.getScopes()))
                .ipAllowlist(ipsToArray(apiKey.getIpAllowlist()))
                .policyRpm(apiKey.getPolicy().rpm())
                .policyRpd(apiKey.getPolicy().rpd())
                .policySse(apiKey.getPolicy().sseConcurrent())
                .createdAt(apiKey.getCreatedAt())
                .revokedAt(apiKey.getRevokedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .build();
    }

    @Override
    public ApiKey toDomain(ApiKeyEntity entity) {
        return ApiKey.restore(
                ApiKeyId.of(entity.getId()),
                AccountId.of(entity.getAccountId()),
                entity.getLabel(),
                ApiKeyPrefix.of(entity.getPrefix()),
                ApiKeyHash.of(entity.getHash()),
                arrayToScopes(entity.getScopes()),
                arrayToIps(entity.getIpAllowlist()),
                new RateLimitPolicy(entity.getPolicyRpm(), entity.getPolicyRpd(), entity.getPolicySse()),
                entity.getCreatedAt(),
                entity.getRevokedAt(),
                entity.getLastUsedAt()
        );
    }

    private static String[] scopesToArray(Set<ApiKeyScope> scopes) {
        if (scopes == null || scopes.isEmpty()) return EMPTY_STRING_ARRAY;
        return scopes.stream().map(Enum::name).toArray(String[]::new);
    }

    private static String[] ipsToArray(Set<String> ips) {
        if (ips == null || ips.isEmpty()) return EMPTY_STRING_ARRAY;
        return ips.toArray(String[]::new);
    }

    private static Set<ApiKeyScope> arrayToScopes(String[] arr) {
        if (arr == null || arr.length == 0) return Collections.emptySet();
        Set<ApiKeyScope> out = EnumSet.noneOf(ApiKeyScope.class);
        for (String s : arr) out.add(ApiKeyScope.valueOf(s));
        return out;
    }

    private static Set<String> arrayToIps(String[] arr) {
        if (arr == null || arr.length == 0) return Collections.emptySet();
        return new LinkedHashSet<>(Arrays.asList(arr));
    }
}
