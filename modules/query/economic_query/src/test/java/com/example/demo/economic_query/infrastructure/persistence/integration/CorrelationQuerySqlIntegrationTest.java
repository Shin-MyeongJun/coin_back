package com.example.demo.economic_query.infrastructure.persistence.integration;

import com.example.demo.economic_query.application.dto.CorrelationResultView;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetCorrelationResultAdapter;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
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

@Import({GetCorrelationResultAdapter.class, IndicatorViewMapper.class})
class CorrelationQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetCorrelationResultAdapter adapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM asset_indicator_correlation", Map.of());
        insertCorrelation(1L, "BTC", "CPIAUCSL", "0.45", 90, 1_704_412_800_000L);
        insertCorrelation(2L, "BTC", "UNRATE", "-0.30", 90, 1_704_412_800_000L);
        insertCorrelation(3L, "BTC", "GDP", "0.65", 180, 1_704_412_800_000L);
        insertCorrelation(4L, "ETH", "CPIAUCSL", "0.20", 90, 1_704_412_800_000L);
        insertCorrelation(5L, "ETH", "UNRATE", "-0.15", 90, 1_704_412_800_000L);
    }

    @Test
    @DisplayName("findByAssetSymbol returns every correlation row for a given asset")
    void findByAssetSymbolBtc() {
        List<CorrelationResultView> rows = adapter.findByAssetSymbol("BTC");

        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(CorrelationResultView::indicatorCode)
                .containsExactlyInAnyOrder("CPIAUCSL", "UNRATE", "GDP");
    }

    @Test
    @DisplayName("findByAssetSymbol isolates by asset_symbol")
    void findByAssetSymbolEth() {
        List<CorrelationResultView> rows = adapter.findByAssetSymbol("ETH");

        assertThat(rows).hasSize(2);
        assertThat(rows).allMatch(v -> v.assetSymbol().equals("ETH"));
    }

    @Test
    @DisplayName("findByAssetSymbol returns empty when asset is unknown")
    void findByAssetSymbolMissing() {
        assertThat(adapter.findByAssetSymbol("DOGE")).isEmpty();
    }

    @Test
    @DisplayName("correlation view preserves correlation value and period_days")
    void correlationViewFields() {
        List<CorrelationResultView> rows = adapter.findByAssetSymbol("BTC");

        CorrelationResultView gdp = rows.stream()
                .filter(r -> r.indicatorCode().equals("GDP"))
                .findFirst().orElseThrow();
        assertThat(gdp.correlation()).isEqualByComparingTo("0.65");
        assertThat(gdp.periodDays()).isEqualTo(180);
        assertThat(gdp.calculatedAt()).isEqualTo(1_704_412_800_000L);

        CorrelationResultView unrate = rows.stream()
                .filter(r -> r.indicatorCode().equals("UNRATE"))
                .findFirst().orElseThrow();
        assertThat(unrate.correlation()).isEqualByComparingTo("-0.30");
    }

    private void insertCorrelation(Long id, String assetSymbol, String indicatorCode,
                                   String correlation, Integer periodDays, Long calculatedAt) {
        jdbc.update(
                "INSERT INTO asset_indicator_correlation (id, asset_symbol, indicator_code, correlation, "
                        + "period_days, calculated_at) VALUES (:id, :sym, :code, :corr, :period, :ts)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("sym", assetSymbol).addValue("code", indicatorCode)
                        .addValue("corr", new BigDecimal(correlation))
                        .addValue("period", periodDays).addValue("ts", calculatedAt)
        );
    }
}
