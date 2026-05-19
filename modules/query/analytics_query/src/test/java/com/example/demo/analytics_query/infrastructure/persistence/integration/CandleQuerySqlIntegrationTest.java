package com.example.demo.analytics_query.infrastructure.persistence.integration;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView;
import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetPremiumCandleMiniChartAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetPremiumCandleSeriesAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetPremiumDetailCandleSeriesAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetTickCandleMiniChartAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetTickCandleSeriesAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.PremiumDetailCandleViewMapper;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickCandleViewMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        GetTickCandleSeriesAdapter.class,
        GetTickCandleMiniChartAdapter.class,
        GetPremiumCandleSeriesAdapter.class,
        GetPremiumCandleMiniChartAdapter.class,
        GetPremiumDetailCandleSeriesAdapter.class,
        TickCandleViewMapper.class,
        PremiumCandleViewMapper.class,
        PremiumDetailCandleViewMapper.class
})
class CandleQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetTickCandleSeriesAdapter tickSeriesAdapter;

    @Autowired
    private GetTickCandleMiniChartAdapter tickMiniAdapter;

    @Autowired
    private GetPremiumCandleSeriesAdapter premiumSeriesAdapter;

    @Autowired
    private GetPremiumCandleMiniChartAdapter premiumMiniAdapter;

    @Autowired
    private GetPremiumDetailCandleSeriesAdapter detailSeriesAdapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM tick_candle", Map.of());
        jdbc.update("DELETE FROM premium_candle", Map.of());
        jdbc.update("DELETE FROM premium_detail_candle", Map.of());

        insertTickCandle(1L, 10L, "1m", "100", "110", "90", "105", 1_700_000_000_000L, 1_700_000_059_999L);
        insertTickCandle(2L, 10L, "1m", "105", "115", "100", "112", 1_700_000_060_000L, 1_700_000_119_999L);
        insertTickCandle(3L, 10L, "1m", "112", "120", "108", "118", 1_700_000_120_000L, 1_700_000_179_999L);
        insertTickCandle(4L, 10L, "5m", "100", "120", "90", "118", 1_700_000_000_000L, 1_700_000_299_999L);
        insertTickCandle(5L, 20L, "1m", "2000", "2100", "1990", "2050", 1_700_000_000_000L, 1_700_000_059_999L);

        insertPremiumCandle(11L, "BTC-KRW", 1L, 2L, "1m",
                "0.02", "0.03", "0.018", "0.025",
                1_700_000_000_000L, 1_700_000_059_999L);
        insertPremiumCandle(12L, "BTC-KRW", 1L, 2L, "1m",
                "0.025", "0.04", "0.022", "0.038",
                1_700_000_060_000L, 1_700_000_119_999L);
        insertPremiumCandle(13L, "BTC-KRW", 1L, 2L, "1m",
                "0.038", "0.042", "0.030", "0.035",
                1_700_000_120_000L, 1_700_000_179_999L);

        insertPremiumDetailCandle(21L, "BTC-KRW", 1L, 2L, "1m",
                1_700_000_000_000L, 1_700_000_059_999L);
        insertPremiumDetailCandle(22L, "BTC-KRW", 1L, 2L, "1m",
                1_700_000_060_000L, 1_700_000_119_999L);
    }

    @Test
    @DisplayName("tick candle series filters by marketCodeId, interval and bucket_open_ts range")
    void tickSeriesFiltersByRange() {
        List<TickCandleView> series = tickSeriesAdapter.findSeries(
                10L, "1m", 1_700_000_000_000L, 1_700_000_119_999L);

        assertThat(series).hasSize(2);
        assertThat(series).extracting(TickCandleView::bucketOpenTs)
                .containsExactlyInAnyOrder(1_700_000_000_000L, 1_700_000_060_000L);
        assertThat(series).allMatch(v -> v.interval().equals("1m"));
        assertThat(series).allMatch(v -> v.marketCodeId() == 10L);
    }

    @Test
    @DisplayName("tick candle series isolates by interval (1m vs 5m)")
    void tickSeriesIsolatesByInterval() {
        List<TickCandleView> fiveM = tickSeriesAdapter.findSeries(
                10L, "5m", 0L, Long.MAX_VALUE);
        assertThat(fiveM).hasSize(1);
        assertThat(fiveM.get(0).interval()).isEqualTo("5m");
    }

    @Test
    @DisplayName("tick candle mini chart returns last N rows ordered by bucket_open_ts desc")
    void tickMiniChartReturnsLastNDesc() {
        List<TickCandleView> result = tickMiniAdapter.findTopN(10L, "1m", 2);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TickCandleView::bucketOpenTs)
                .containsExactly(1_700_000_120_000L, 1_700_000_060_000L);
    }

    @Test
    @DisplayName("tick candle cursor BACKWARD returns ascending after internal reverse")
    void tickCursorBackwardAscendingAfterReverse() {
        List<TickCandleView> result = tickSeriesAdapter.findCursor(
                10L, "1m", 1_700_000_120_000L, 2, CursorDirection.BACKWARD);

        // candidates ≤ cursor desc: 1_700_000_120_000, 1_700_000_060_000, 1_700_000_000_000
        // limit=2 → [120000, 60000] → reversed → [60000, 120000]
        assertThat(result).extracting(TickCandleView::bucketOpenTs)
                .containsExactly(1_700_000_060_000L, 1_700_000_120_000L);
    }

    @Test
    @DisplayName("tick candle cursor FORWARD returns ascending starting from cursor")
    void tickCursorForwardAscending() {
        List<TickCandleView> result = tickSeriesAdapter.findCursor(
                10L, "1m", 1_700_000_060_000L, 5, CursorDirection.FORWARD);

        assertThat(result).extracting(TickCandleView::bucketOpenTs)
                .containsExactly(1_700_000_060_000L, 1_700_000_120_000L);
    }

    @Test
    @DisplayName("premium candle series filters by (symbol, baseExchangeId, compareExchangeId, interval, range)")
    void premiumSeriesFiltersByCompositeKey() {
        List<PremiumCandleView> result = premiumSeriesAdapter.findSeries(
                "BTC-KRW", 1L, 2L, "1m",
                1_700_000_000_000L, 1_700_000_120_000L);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(v -> v.symbol().equals("BTC-KRW"));
        assertThat(result).allMatch(v -> v.baseExchangeId() == 1L);
        assertThat(result).allMatch(v -> v.compareExchangeId() == 2L);
    }

    @Test
    @DisplayName("premium candle mini chart returns last N rows by bucket_open_ts desc")
    void premiumMiniChartTopN() {
        List<PremiumCandleView> result = premiumMiniAdapter.findTopN(
                "BTC-KRW", 1L, 2L, "1m", 2);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PremiumCandleView::bucketOpenTs)
                .containsExactly(1_700_000_120_000L, 1_700_000_060_000L);
    }

    @Test
    @DisplayName("premium-detail candle series filters by composite key and bucket_close_ts range")
    void premiumDetailSeriesFiltersByCompositeKey() {
        List<PremiumDetailCandleView> result = detailSeriesAdapter.findSeries(
                "BTC-KRW", 1L, 2L, "1m",
                1_700_000_000_000L, 1_700_000_119_999L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(v -> v.symbol().equals("BTC-KRW"));
        // bucket_close_ts ASC ordering
        assertThat(result).extracting(PremiumDetailCandleView::bucketCloseTs)
                .containsExactly(1_700_000_059_999L, 1_700_000_119_999L);
    }

    private void insertTickCandle(Long id, Long mc, String interval,
                                  String open, String high, String low, String close,
                                  Long bucketOpen, Long bucketClose) {
        jdbc.update(
                "INSERT INTO tick_candle (id, market_code_id, interval, open, high, low, close, "
                        + "bucket_open_ts, bucket_close_ts, observe_open_ts, observe_close_ts) "
                        + "VALUES (:id, :mc, :iv, :open, :high, :low, :close, :bo, :bc, :oo, :oc)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("mc", mc).addValue("iv", interval)
                        .addValue("open", new BigDecimal(open)).addValue("high", new BigDecimal(high))
                        .addValue("low", new BigDecimal(low)).addValue("close", new BigDecimal(close))
                        .addValue("bo", bucketOpen).addValue("bc", bucketClose)
                        .addValue("oo", bucketOpen).addValue("oc", bucketClose)
        );
    }

    private void insertPremiumCandle(Long id, String symbol, Long baseEx, Long cmpEx, String interval,
                                     String open, String high, String low, String close,
                                     Long bucketOpen, Long bucketClose) {
        jdbc.update(
                "INSERT INTO premium_candle (id, symbol, base_exchange_id, compare_exchange_id, interval, "
                        + "open, high, low, close, bucket_open_ts, bucket_close_ts, observe_open_ts, observe_close_ts) "
                        + "VALUES (:id, :sym, :be, :ce, :iv, :open, :high, :low, :close, :bo, :bc, :oo, :oc)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("sym", symbol).addValue("be", baseEx).addValue("ce", cmpEx)
                        .addValue("iv", interval)
                        .addValue("open", new BigDecimal(open)).addValue("high", new BigDecimal(high))
                        .addValue("low", new BigDecimal(low)).addValue("close", new BigDecimal(close))
                        .addValue("bo", bucketOpen).addValue("bc", bucketClose)
                        .addValue("oo", bucketOpen).addValue("oc", bucketClose)
        );
    }

    private void insertPremiumDetailCandle(Long id, String symbol, Long baseEx, Long cmpEx, String interval,
                                           Long bucketOpen, Long bucketClose) {
        BigDecimal one = new BigDecimal("1.0");
        jdbc.update(
                "INSERT INTO premium_detail_candle (id, symbol, base_exchange_id, compare_exchange_id, interval, "
                        + "open_base_price, open_base_quote_val, open_compare_price, open_compare_quote_val, "
                        + "high_base_price, high_base_quote_val, high_compare_price, high_compare_quote_val, "
                        + "low_base_price, low_base_quote_val, low_compare_price, low_compare_quote_val, "
                        + "close_base_price, close_base_quote_val, close_compare_price, close_compare_quote_val, "
                        + "bucket_open_ts, bucket_close_ts, observe_open_ts, observe_close_ts) "
                        + "VALUES (:id, :sym, :be, :ce, :iv, "
                        + ":v, :v, :v, :v, "
                        + ":v, :v, :v, :v, "
                        + ":v, :v, :v, :v, "
                        + ":v, :v, :v, :v, "
                        + ":bo, :bc, :oo, :oc)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("sym", symbol).addValue("be", baseEx).addValue("ce", cmpEx)
                        .addValue("iv", interval).addValue("v", one)
                        .addValue("bo", bucketOpen).addValue("bc", bucketClose)
                        .addValue("oo", bucketOpen).addValue("oc", bucketClose)
        );
    }
}
