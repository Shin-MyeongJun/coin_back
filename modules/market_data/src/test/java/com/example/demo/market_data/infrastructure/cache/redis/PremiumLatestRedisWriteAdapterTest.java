package com.example.demo.market_data.infrastructure.cache.redis;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵하세요.

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.market_data.domain.domain.Premium;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Testcontainers(disabledWithoutDocker = true)
class PremiumLatestRedisWriteAdapterTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static StringRedisTemplate redisTemplate;

    PremiumLatestRedisWriteAdapter sut;

    @BeforeAll
    static void setUpRedis() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                redis.getHost(), redis.getMappedPort(6379));
        factory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(factory);
        redisTemplate.afterPropertiesSet();
    }

    @BeforeEach
    void setUp() {
        sut = new PremiumLatestRedisWriteAdapter(redisTemplate);
        ReflectionTestUtils.setField(sut, "env", "local");
        redisTemplate.execute((RedisCallback<Void>) conn -> { conn.serverCommands().flushAll(); return null; });
    }

    private static Premium premium(String symbol, long baseId, long compareId, String bid, String ask) {
        return new Premium(symbol, baseId, compareId,
                new BigDecimal(bid), new BigDecimal(ask), 7_777L);
    }

    @Test
    @DisplayName("upsertAll — 키가 RedisKeys.premiumLatest('local', baseEx, compareEx, symbol) 와 정확히 일치")
    void upsertAll_keyMatchesRedisKeys() {
        // given
        Premium p = premium("BTC", 10L, 20L, "1.23456789", "1.98765432");
        String expectedKey = RedisKeys.premiumLatest("local", 10L, 20L, "BTC");

        // when
        sut.upsertAll(List.of(p));

        // then
        assertThat(redisTemplate.hasKey(expectedKey)).isTrue();
    }

    @Test
    @DisplayName("upsertAll — bid·ask·ts 필드 값이 정확히 저장됨")
    void upsertAll_fieldsStoredCorrectly() {
        // given
        Premium p = premium("BTC", 10L, 20L, "1.23456789", "1.98765432");
        String key = RedisKeys.premiumLatest("local", 10L, 20L, "BTC");

        // when
        sut.upsertAll(List.of(p));

        // then
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(key);
        assertThat(fields)
                .containsEntry("bid", p.bid().toPlainString())
                .containsEntry("ask", p.ask().toPlainString())
                .containsEntry("ts",  Long.toString(p.timestamp()));
    }

    @Test
    @DisplayName("upsertAll — TTL이 설정됨(양수)")
    void upsertAll_ttlIsPositive() {
        // given
        Premium p = premium("ETH", 10L, 30L, "0.5", "0.6");
        String key = RedisKeys.premiumLatest("local", 10L, 30L, "ETH");

        // when
        sut.upsertAll(List.of(p));

        // then
        assertThat(redisTemplate.getExpire(key)).isPositive();
    }

    @Test
    @DisplayName("upsertAll — 빈 리스트 전달 시 키 미생성(no-op)")
    void upsertAll_emptyList_noKeyCreated() {
        // given
        String key = RedisKeys.premiumLatest("local", 10L, 20L, "BTC");

        // when
        sut.upsertAll(List.of());

        // then
        assertThat(redisTemplate.hasKey(key)).isFalse();
    }

    @Test
    @DisplayName("upsert(null) — 예외 없이 무시됨")
    void upsert_null_noException() {
        assertThatNoException().isThrownBy(() -> sut.upsert(null));
    }
}
