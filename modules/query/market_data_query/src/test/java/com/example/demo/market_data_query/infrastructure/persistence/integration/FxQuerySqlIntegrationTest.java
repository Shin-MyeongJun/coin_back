package com.example.demo.market_data_query.infrastructure.persistence.integration;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetFxDownsampledAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetFxRawAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetLatestFxAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.FxViewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
        GetLatestFxAdapter.class,
        GetFxRawAdapter.class,
        GetFxDownsampledAdapter.class,
        FxViewMapper.class
})
class FxQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetLatestFxAdapter latestAdapter;

    @Autowired
    private GetFxRawAdapter rawAdapter;

    @Autowired
    private GetFxDownsampledAdapter downsampledAdapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM fx", Map.of());
        insertFx(1L, "USD", "KRW", "1300.0", 1_700_000_000_000L);
        insertFx(2L, "USD", "KRW", "1301.5", 1_700_000_001_000L);
        insertFx(3L, "USD", "KRW", "1305.0", 1_700_000_002_000L);
        insertFx(4L, "USD", "KRW", "1310.0", 1_700_000_005_000L);
        insertFx(5L, "USD", "JPY", "150.0", 1_700_000_001_000L);
        insertFx(6L, "EUR", "USD", "1.08", 1_700_000_001_000L);
    }

    @Test
    @DisplayName("findLatest returns the latest fx row for (base, quote)")
    void findLatestReturnsTopByTimestampDesc() {
        Optional<FxView> usdKrw = latestAdapter.findLatest("USD", "KRW");

        assertThat(usdKrw).isPresent();
        assertThat(usdKrw.get().rate()).isEqualByComparingTo("1310.0");
        assertThat(usdKrw.get().timestamp()).isEqualTo(1_700_000_005_000L);
    }

    @Test
    @DisplayName("findLatest returns empty for unknown currency pair")
    void findLatestReturnsEmptyForUnknownPair() {
        assertThat(latestAdapter.findLatest("ABC", "XYZ")).isEmpty();
    }

    @Test
    @DisplayName("findRaw returns rows within inclusive timestamp range")
    void rawRangeReturnsRowsWithinRange() {
        List<FxView> rows = rawAdapter.findRaw("USD", "KRW",
                1_700_000_001_000L, 1_700_000_002_000L);

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(FxView::rate)
                .extracting(BigDecimal::doubleValue)
                .containsExactlyInAnyOrder(1301.5, 1305.0);
    }

    @Test
    @DisplayName("findRaw isolates by (base, quote)")
    void rawRangeIsolatesByPair() {
        List<FxView> usdKrw = rawAdapter.findRaw("USD", "KRW", 0L, Long.MAX_VALUE);
        List<FxView> usdJpy = rawAdapter.findRaw("USD", "JPY", 0L, Long.MAX_VALUE);

        assertThat(usdKrw).hasSize(4);
        assertThat(usdJpy).hasSize(1);
        assertThat(usdJpy.get(0).rate()).isEqualByComparingTo("150.0");
    }

    @Test
    @DisplayName("findCursor BACKWARD returns ascending order after internal reverse, capped by limit")
    void cursorBackwardReturnsAscendingOrder() {
        List<FxView> rows = rawAdapter.findCursor("USD", "KRW",
                1_700_000_005_000L, 2, CursorDirection.BACKWARD);

        // candidates ≤ cursor desc: 1310,1305,1301.5,1300. limit 2 → 1310,1305. reversed → 1305, 1310
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(FxView::timestamp)
                .containsExactly(1_700_000_002_000L, 1_700_000_005_000L);
    }

    @Test
    @DisplayName("findCursor FORWARD returns ascending order starting from cursor, capped by limit")
    void cursorForwardReturnsAscendingOrder() {
        List<FxView> rows = rawAdapter.findCursor("USD", "KRW",
                1_700_000_001_000L, 2, CursorDirection.FORWARD);

        // candidates ≥ cursor asc: 1301.5, 1305, 1310. limit 2 → 1301.5, 1305
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(FxView::timestamp)
                .containsExactly(1_700_000_001_000L, 1_700_000_002_000L);
    }

    @Test
    @Disabled("TimescaleDB time_bucket required for fx downsampled bucket aggregation")
    @DisplayName("findDownsampled - TimescaleDB only")
    void downsampledBucketRequiresTimescaleDb() {
        List<FxView> rows = downsampledAdapter.findDownsampled("USD", "KRW", 1,
                1_700_000_000_000L, 1_700_000_005_000L);
        assertThat(rows).isNotNull();
    }

    private void insertFx(Long id, String base, String quote, String rate, Long timestamp) {
        jdbc.update(
                "INSERT INTO fx (id, base_currency, quote_currency, rate, timestamp) "
                        + "VALUES (:id, :base, :quote, :rate, :ts)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("base", base)
                        .addValue("quote", quote)
                        .addValue("rate", new BigDecimal(rate))
                        .addValue("ts", timestamp)
        );
    }
}
