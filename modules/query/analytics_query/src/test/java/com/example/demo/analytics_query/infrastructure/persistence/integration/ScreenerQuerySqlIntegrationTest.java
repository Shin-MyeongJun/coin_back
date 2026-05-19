package com.example.demo.analytics_query.infrastructure.persistence.integration;

import com.example.demo.analytics_query.application.dto.PremiumScreenerResult;
import com.example.demo.analytics_query.application.dto.ScreenerCondition;
import com.example.demo.analytics_query.application.dto.TickScreenerResult;
import com.example.demo.analytics_query.infrastructure.config.QueryDslConfig;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetScreenerAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.querydsl.IndicatorQueryDslRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        GetScreenerAdapter.class,
        IndicatorQueryDslRepository.class,
        QueryDslConfig.class
})
class ScreenerQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetScreenerAdapter adapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM tick_indicator", Map.of());
        jdbc.update("DELETE FROM premium_indicator", Map.of());

        // tick: each marketCode has at most one RSI/14 row and one MACD/12 row at 1m
        // marketCode 10: RSI=65 (matches RSI>=60), MACD=0.5 (matches MACD>=0)
        insertTickIndicator(1L, 10L, "1m", "RSI", 14, "65.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertTickIndicator(2L, 10L, "1m", "MACD", 12, "0.5",
                1_700_000_120_000L, 1_700_000_179_999L);
        // marketCode 20: RSI=70 (matches), MACD=-0.5 (does NOT match)
        insertTickIndicator(3L, 20L, "1m", "RSI", 14, "70.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertTickIndicator(4L, 20L, "1m", "MACD", 12, "-0.5",
                1_700_000_120_000L, 1_700_000_179_999L);
        // marketCode 30: RSI=40 (does NOT match), MACD=0.5 (matches)
        insertTickIndicator(5L, 30L, "1m", "RSI", 14, "40.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertTickIndicator(6L, 30L, "1m", "MACD", 12, "0.5",
                1_700_000_120_000L, 1_700_000_179_999L);
        // marketCode 40: RSI=80 (matches), MACD=1.0 (matches)
        insertTickIndicator(7L, 40L, "1m", "RSI", 14, "80.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertTickIndicator(8L, 40L, "1m", "MACD", 12, "1.0",
                1_700_000_120_000L, 1_700_000_179_999L);

        // premium: BTC-KRW (1,2) matches both, ETH-KRW (1,2) only RSI, BTC-KRW (1,3) only MACD
        insertPremiumIndicator(11L, "BTC-KRW", 1L, 2L, "1m", "RSI", 14, "70.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertPremiumIndicator(12L, "BTC-KRW", 1L, 2L, "1m", "MACD", 12, "0.5",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertPremiumIndicator(13L, "ETH-KRW", 1L, 2L, "1m", "RSI", 14, "75.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertPremiumIndicator(14L, "ETH-KRW", 1L, 2L, "1m", "MACD", 12, "-0.3",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertPremiumIndicator(15L, "BTC-KRW", 1L, 3L, "1m", "RSI", 14, "30.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        insertPremiumIndicator(16L, "BTC-KRW", 1L, 3L, "1m", "MACD", 12, "0.7",
                1_700_000_120_000L, 1_700_000_179_999L);
    }

    @Test
    @DisplayName("tick screener intersects marketCodeId sets across all conditions")
    void tickScreenerIntersects() {
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition("1m", "RSI", 14, new BigDecimal("60"), null),
                new ScreenerCondition("1m", "MACD", 12, BigDecimal.ZERO, null)
        );

        List<TickScreenerResult> result = adapter.findTickByConditions(conditions);

        // Only marketCode 10, 40 satisfy both
        // Result is the first condition's rows filtered by intersection → RSI rows for 10, 40
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TickScreenerResult::marketCodeId)
                .containsExactlyInAnyOrder(10L, 40L);
        assertThat(result).allMatch(r -> r.type().equals("RSI"));
    }

    @Test
    @DisplayName("tick screener returns empty when no marketCode satisfies all conditions")
    void tickScreenerEmptyWhenNoIntersection() {
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition("1m", "RSI", 14, new BigDecimal("90"), null),
                new ScreenerCondition("1m", "MACD", 12, BigDecimal.ZERO, null)
        );

        List<TickScreenerResult> result = adapter.findTickByConditions(conditions);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("tick screener single condition returns rows that satisfy it")
    void tickScreenerSingleCondition() {
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition("1m", "RSI", 14, new BigDecimal("60"), null)
        );

        List<TickScreenerResult> result = adapter.findTickByConditions(conditions);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(TickScreenerResult::marketCodeId)
                .containsExactlyInAnyOrder(10L, 20L, 40L);
    }

    @Test
    @DisplayName("tick screener with empty conditions returns empty list")
    void tickScreenerEmptyConditions() {
        assertThat(adapter.findTickByConditions(List.of())).isEmpty();
    }

    @Test
    @DisplayName("tick screener respects period filter")
    void tickScreenerRespectsPeriod() {
        // No tick rows with period=99 exist, so intersection is empty
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition("1m", "RSI", 99, null, null)
        );

        assertThat(adapter.findTickByConditions(conditions)).isEmpty();
    }

    @Test
    @DisplayName("premium screener intersects on (symbol, baseEx, compareEx) tuple")
    void premiumScreenerIntersects() {
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition("1m", "RSI", 14, new BigDecimal("60"), null),
                new ScreenerCondition("1m", "MACD", 12, BigDecimal.ZERO, null)
        );

        List<PremiumScreenerResult> result = adapter.findPremiumByConditions(conditions);

        // Only BTC-KRW(1,2) matches RSI≥60 AND MACD≥0
        assertThat(result).hasSize(1);
        PremiumScreenerResult only = result.get(0);
        assertThat(only.symbol()).isEqualTo("BTC-KRW");
        assertThat(only.baseExchangeId()).isEqualTo(1L);
        assertThat(only.compareExchangeId()).isEqualTo(2L);
        assertThat(only.type()).isEqualTo("RSI");
    }

    @Test
    @DisplayName("premium screener with maxValue threshold filters upper bound")
    void premiumScreenerWithMaxValue() {
        List<ScreenerCondition> conditions = List.of(
                new ScreenerCondition("1m", "RSI", 14, null, new BigDecimal("40"))
        );

        List<PremiumScreenerResult> result = adapter.findPremiumByConditions(conditions);

        // Only BTC-KRW(1,3) has RSI<=40
        assertThat(result).hasSize(1);
        assertThat(result.get(0).compareExchangeId()).isEqualTo(3L);
    }

    private void insertTickIndicator(Long id, Long mc, String interval, String type, int period,
                                     String value, Long bucketOpen, Long bucketClose) {
        jdbc.update(
                "INSERT INTO tick_indicator (id, market_code_id, interval, type, period, value, "
                        + "bucket_open_ts, bucket_close_ts, observe_open_ts, observe_close_ts) "
                        + "VALUES (:id, :mc, :iv, :tp, :pe, :val, :bo, :bc, :oo, :oc)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("mc", mc).addValue("iv", interval)
                        .addValue("tp", type).addValue("pe", period).addValue("val", new BigDecimal(value))
                        .addValue("bo", bucketOpen).addValue("bc", bucketClose)
                        .addValue("oo", bucketOpen).addValue("oc", bucketClose)
        );
    }

    private void insertPremiumIndicator(Long id, String symbol, Long baseEx, Long cmpEx,
                                        String interval, String type, int period, String value,
                                        Long bucketOpen, Long bucketClose) {
        jdbc.update(
                "INSERT INTO premium_indicator (id, symbol, base_exchange_id, compare_exchange_id, "
                        + "interval, type, period, value, "
                        + "bucket_open_ts, bucket_close_ts, observe_open_ts, observe_close_ts) "
                        + "VALUES (:id, :sym, :be, :ce, :iv, :tp, :pe, :val, :bo, :bc, :oo, :oc)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("sym", symbol).addValue("be", baseEx).addValue("ce", cmpEx)
                        .addValue("iv", interval).addValue("tp", type).addValue("pe", period)
                        .addValue("val", new BigDecimal(value))
                        .addValue("bo", bucketOpen).addValue("bc", bucketClose)
                        .addValue("oo", bucketOpen).addValue("oc", bucketClose)
        );
    }
}
