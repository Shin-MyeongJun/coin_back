package com.example.demo.analytics_query.infrastructure.persistence.integration;

import com.example.demo.analytics_query.application.dto.PremiumIndicatorView;
import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetLatestIndicatorMultiMarketAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetPremiumIndicatorSeriesAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetPremiumLatestIndicatorAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetTickIndicatorSeriesAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetTickLatestIndicatorAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumIndicatorViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickIndicatorViewMapper;
import com.example.demo.infra_shard.paging.CursorDirection;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        GetTickLatestIndicatorAdapter.class,
        GetTickIndicatorSeriesAdapter.class,
        GetLatestIndicatorMultiMarketAdapter.class,
        GetPremiumLatestIndicatorAdapter.class,
        GetPremiumIndicatorSeriesAdapter.class,
        TickIndicatorViewMapper.class,
        PremiumIndicatorViewMapper.class
})
class IndicatorQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetTickLatestIndicatorAdapter tickLatestAdapter;

    @Autowired
    private GetTickIndicatorSeriesAdapter tickSeriesAdapter;

    @Autowired
    private GetLatestIndicatorMultiMarketAdapter multiMarketAdapter;

    @Autowired
    private GetPremiumLatestIndicatorAdapter premiumLatestAdapter;

    @Autowired
    private GetPremiumIndicatorSeriesAdapter premiumSeriesAdapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM tick_indicator", Map.of());
        jdbc.update("DELETE FROM premium_indicator", Map.of());

        // tick: marketCode 10, RSI/14, interval=1m, 3 buckets
        insertTickIndicator(1L, 10L, "1m", "RSI", 14, "55.0",
                1_700_000_000_000L, 1_700_000_059_999L);
        insertTickIndicator(2L, 10L, "1m", "RSI", 14, "60.0",
                1_700_000_060_000L, 1_700_000_119_999L);
        insertTickIndicator(3L, 10L, "1m", "RSI", 14, "65.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        // tick: marketCode 10, MACD different type
        insertTickIndicator(4L, 10L, "1m", "MACD", 12, "0.5",
                1_700_000_120_000L, 1_700_000_179_999L);
        // tick: marketCode 20, RSI/14
        insertTickIndicator(5L, 20L, "1m", "RSI", 14, "30.0",
                1_700_000_060_000L, 1_700_000_119_999L);
        insertTickIndicator(6L, 20L, "1m", "RSI", 14, "40.0",
                1_700_000_120_000L, 1_700_000_179_999L);
        // marketCode 30, no RSI
        insertTickIndicator(7L, 30L, "1m", "MACD", 12, "0.1",
                1_700_000_120_000L, 1_700_000_179_999L);

        // premium indicator data
        insertPremiumIndicator(11L, "BTC-KRW", 1L, 2L, "1m", "RSI", 14, "70.0",
                1_700_000_060_000L, 1_700_000_119_999L);
        insertPremiumIndicator(12L, "BTC-KRW", 1L, 2L, "1m", "RSI", 14, "75.0",
                1_700_000_120_000L, 1_700_000_179_999L);
    }

    @Test
    @DisplayName("tick latest indicator returns top-1 by bucket_close_ts desc, id desc")
    void tickLatestIndicator() {
        Optional<TickIndicatorView> latest = tickLatestAdapter.findLatest(10L, "1m", "RSI");

        assertThat(latest).isPresent();
        assertThat(latest.get().value()).isEqualByComparingTo("65.0");
        assertThat(latest.get().bucketCloseTs()).isEqualTo(1_700_000_179_999L);
    }

    @Test
    @DisplayName("tick latest indicator returns empty when (marketCode, interval, type) is unknown")
    void tickLatestIndicatorEmpty() {
        assertThat(tickLatestAdapter.findLatest(10L, "1m", "BBAND")).isEmpty();
        assertThat(tickLatestAdapter.findLatest(9999L, "1m", "RSI")).isEmpty();
    }

    @Test
    @DisplayName("tick indicator series returns rows in bucket_open_ts order within range")
    void tickIndicatorSeries() {
        List<TickIndicatorView> series = tickSeriesAdapter.findSeries(
                10L, "1m", "RSI",
                1_700_000_060_000L, 1_700_000_179_999L);

        assertThat(series).hasSize(2);
        assertThat(series).extracting(TickIndicatorView::value)
                .extracting(BigDecimal::doubleValue)
                .containsExactlyInAnyOrder(60.0, 65.0);
    }

    @Test
    @DisplayName("tick indicator series isolates by type")
    void tickIndicatorSeriesIsolatesByType() {
        List<TickIndicatorView> rsi = tickSeriesAdapter.findSeries(
                10L, "1m", "RSI", 0L, Long.MAX_VALUE);
        List<TickIndicatorView> macd = tickSeriesAdapter.findSeries(
                10L, "1m", "MACD", 0L, Long.MAX_VALUE);

        assertThat(rsi).hasSize(3);
        assertThat(macd).hasSize(1);
        assertThat(rsi).allMatch(v -> v.type().equals("RSI"));
        assertThat(macd).allMatch(v -> v.type().equals("MACD"));
    }

    @Test
    @DisplayName("tick indicator cursor FORWARD ascending starting from cursor")
    void tickIndicatorCursorForward() {
        List<TickIndicatorView> result = tickSeriesAdapter.findCursor(
                10L, "1m", "RSI", 1_700_000_060_000L, 5, CursorDirection.FORWARD);

        assertThat(result).extracting(TickIndicatorView::bucketOpenTs)
                .containsExactly(1_700_000_060_000L, 1_700_000_120_000L);
    }

    @Test
    @DisplayName("findLatestForMarkets bulk returns one row per market_code_id using DISTINCT ON")
    void findLatestForMarketsBulk() {
        List<TickIndicatorView> result = multiMarketAdapter.findLatestForMarkets(
                List.of(10L, 20L, 30L), "1m", "RSI");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TickIndicatorView::marketCodeId)
                .containsExactlyInAnyOrder(10L, 20L);

        TickIndicatorView ten = result.stream()
                .filter(v -> v.marketCodeId() == 10L).findFirst().orElseThrow();
        assertThat(ten.value()).isEqualByComparingTo("65.0");

        TickIndicatorView twenty = result.stream()
                .filter(v -> v.marketCodeId() == 20L).findFirst().orElseThrow();
        assertThat(twenty.value()).isEqualByComparingTo("40.0");
    }

    @Test
    @DisplayName("findLatestForMarkets filters by interval and type")
    void findLatestForMarketsFiltersByType() {
        List<TickIndicatorView> macd = multiMarketAdapter.findLatestForMarkets(
                List.of(10L, 20L, 30L), "1m", "MACD");

        assertThat(macd).extracting(TickIndicatorView::marketCodeId)
                .containsExactlyInAnyOrder(10L, 30L);
    }

    @Test
    @DisplayName("premium latest indicator returns top-1 by bucket_close_ts desc, id desc")
    void premiumLatestIndicator() {
        Optional<PremiumIndicatorView> latest = premiumLatestAdapter.findLatest(
                "BTC-KRW", 1L, 2L, "1m", "RSI");

        assertThat(latest).isPresent();
        assertThat(latest.get().value()).isEqualByComparingTo("75.0");
    }

    @Test
    @DisplayName("premium indicator series filters by composite key and range")
    void premiumIndicatorSeries() {
        List<PremiumIndicatorView> series = premiumSeriesAdapter.findSeries(
                "BTC-KRW", 1L, 2L, "1m", "RSI",
                1_700_000_060_000L, 1_700_000_179_999L);

        assertThat(series).hasSize(2);
        assertThat(series).extracting(PremiumIndicatorView::value)
                .extracting(BigDecimal::doubleValue)
                .containsExactlyInAnyOrder(70.0, 75.0);
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
