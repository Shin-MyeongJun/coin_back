package com.example.demo.market_data.infrastructure.cache.redis;

// NOTE: Docker이 실행 중이어야 합니다. Docker 미가용 환경에서는 이 테스트를 스킵하세요.

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Testcontainers
class PremiumDetailLatestRedisWriteAdapterTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static StringRedisTemplate redisTemplate;

    PremiumDetailLatestRedisWriteAdapter sut;

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
        sut = new PremiumDetailLatestRedisWriteAdapter(redisTemplate);
        redisTemplate.execute((RedisCallback<Void>) conn -> { conn.serverCommands().flushAll(); return null; });
    }

    private static PremiumDetail detail(String symbol, long baseId, long compareId) {
        return new PremiumDetail(symbol, baseId, compareId,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                6_666L);
    }

    @Test
    @DisplayName("upsertAll — 키가 RedisKeys.premiumDetailLatest('local', baseEx, compareEx, symbol) 와 정확히 일치")
    void upsertAll_keyMatchesRedisKeys() {
        // given
        PremiumDetail pd = detail("BTC", 10L, 20L);
        String expectedKey = RedisKeys.premiumDetailLatest("local", 10L, 20L, "BTC");

        // when
        sut.upsertAll(List.of(pd));

        // then
        assertThat(redisTemplate.hasKey(expectedKey)).isTrue();
    }

    @Test
    @DisplayName("upsertAll — 7개 원시 필드(ts·baseBid·baseAsk·baseQuote·compareBid·compareAsk·compareQuote) 전부 저장됨")
    void upsertAll_allRawFieldsStoredCorrectly() {
        // given
        PremiumDetail pd = detail("BTC", 10L, 20L);
        String key = RedisKeys.premiumDetailLatest("local", 10L, 20L, "BTC");

        // when
        sut.upsertAll(List.of(pd));

        // then
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(key);
        assertThat(fields)
                .containsEntry("ts",          Long.toString(pd.timestamp()))
                .containsEntry("baseBid",     pd.baseBid().toPlainString())
                .containsEntry("baseAsk",     pd.baseAsk().toPlainString())
                .containsEntry("baseQuote",   pd.baseQuoteVal().toPlainString())
                .containsEntry("compareBid",  pd.compareBid().toPlainString())
                .containsEntry("compareAsk",  pd.compareAsk().toPlainString())
                .containsEntry("compareQuote",pd.compareQuoteVal().toPlainString());
    }

    @Test
    @DisplayName("upsertAll — TTL이 설정됨(양수)")
    void upsertAll_ttlIsPositive() {
        // given
        PremiumDetail pd = detail("ETH", 10L, 30L);
        String key = RedisKeys.premiumDetailLatest("local", 10L, 30L, "ETH");

        // when
        sut.upsertAll(List.of(pd));

        // then
        assertThat(redisTemplate.getExpire(key)).isPositive();
    }

    @Test
    @DisplayName("upsertAll — 빈 리스트 전달 시 키 미생성(no-op)")
    void upsertAll_emptyList_noKeyCreated() {
        // given
        String key = RedisKeys.premiumDetailLatest("local", 10L, 20L, "BTC");

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
