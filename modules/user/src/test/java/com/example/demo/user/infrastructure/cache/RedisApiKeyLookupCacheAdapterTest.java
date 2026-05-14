package com.example.demo.user.infrastructure.cache;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import com.example.demo.user.domain.domain.ApiKeyHash;
import com.example.demo.user.domain.domain.ApiKeyId;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeyScope;
import com.example.demo.user.domain.domain.RateLimitPolicies;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RedisApiKeyLookupCacheAdapterTest {

    private StringRedisTemplate redis;
    private ValueOperations<String, String> ops;
    private ObjectMapper om;
    private RedisApiKeyLookupCacheAdapter adapter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        redis = mock(StringRedisTemplate.class);
        ops = (ValueOperations<String, String>) mock(ValueOperations.class);
        given(redis.opsForValue()).willReturn(ops);
        om = new ObjectMapper();
        adapter = new RedisApiKeyLookupCacheAdapter(redis, om);
        ReflectionTestUtils.setField(adapter, "env", "test");
    }

    private ApiKey newKey() {
        return ApiKey.issue(
                ApiKeyId.generate(), AccountId.generate(), "lbl",
                ApiKeyPrefix.of("ABCD1234"),
                ApiKeyHash.of("$2a$12$AAAAAAAAAAAAAAAAAAAAAA"),
                EnumSet.of(ApiKeyScope.READ_MARKET),
                Set.of("10.0.0.1"),
                RateLimitPolicies.FREE,
                Instant.parse("2026-05-14T00:00:00Z")
        );
    }

    @Test
    void put_then_get_round_trips_via_json() throws Exception {
        ApiKey original = newKey();
        String[] capturedJson = new String[1];

        // capture the JSON written
        org.mockito.BDDMockito.willAnswer(inv -> {
            capturedJson[0] = inv.getArgument(1);
            return null;
        }).given(ops).set(any(), any(), any(java.time.Duration.class));

        adapter.put(original);

        assertThat(capturedJson[0]).isNotNull().contains("ABCD1234");

        given(ops.get(eq("ys:test:v1:apikey:lookup:ABCD1234"))).willReturn(capturedJson[0]);

        Optional<ApiKey> roundTripped = adapter.get(original.getPrefix());
        assertThat(roundTripped).isPresent();
        assertThat(roundTripped.get().getId()).isEqualTo(original.getId());
        assertThat(roundTripped.get().getHash()).isEqualTo(original.getHash());
        assertThat(roundTripped.get().getScopes()).containsExactly(ApiKeyScope.READ_MARKET);
        assertThat(roundTripped.get().getIpAllowlist()).containsExactly("10.0.0.1");
    }

    @Test
    void get_returns_empty_when_redis_returns_null() {
        given(ops.get(any())).willReturn(null);
        assertThat(adapter.get(ApiKeyPrefix.of("ZZZZZZZZ"))).isEmpty();
    }

    @Test
    void get_returns_empty_and_evicts_on_corrupt_json() {
        given(ops.get(eq("ys:test:v1:apikey:lookup:ABCD1234"))).willReturn("not-json");

        assertThat(adapter.get(ApiKeyPrefix.of("ABCD1234"))).isEmpty();
        verify(redis).delete("ys:test:v1:apikey:lookup:ABCD1234");
    }

    @Test
    void evict_deletes_redis_key() {
        adapter.evict(ApiKeyPrefix.of("ABCD1234"));
        verify(redis).delete("ys:test:v1:apikey:lookup:ABCD1234");
    }
}
