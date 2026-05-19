package com.example.demo.economic_query.infrastructure.persistence.integration;

import com.example.demo.economic_query.application.dto.IndicatorChangeRateView;
import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.dto.IndicatorSeriesView;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetIndicatorChangeRateAdapter;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetIndicatorListByCategoryAdapter;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetIndicatorMetaAdapter;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetIndicatorSeriesAdapter;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        GetIndicatorSeriesAdapter.class,
        GetIndicatorMetaAdapter.class,
        GetIndicatorListByCategoryAdapter.class,
        GetIndicatorChangeRateAdapter.class,
        IndicatorViewMapper.class
})
class IndicatorSeriesQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetIndicatorSeriesAdapter seriesAdapter;

    @Autowired
    private GetIndicatorMetaAdapter metaAdapter;

    @Autowired
    private GetIndicatorListByCategoryAdapter categoryAdapter;

    @Autowired
    private GetIndicatorChangeRateAdapter changeRateAdapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM economic_indicator", Map.of());
        jdbc.update("DELETE FROM economic_indicator_code", Map.of());

        insertCode(1L, "CPIAUCSL", "US", "INFLATION", "monthly", "Index");
        insertCode(2L, "UNRATE", "US", "EMPLOYMENT", "monthly", "Percent");
        insertCode(3L, "GDP", "US", "GROWTH", "quarterly", "Billion USD");

        insertIndicator(101L, 1L, "100.0", 20240101L, 20240105L, 1_704_412_800_000L);
        insertIndicator(102L, 1L, "101.5", 20240201L, 20240205L, 1_707_091_200_000L);
        insertIndicator(103L, 1L, "103.0", 20240301L, 20240305L, 1_709_596_800_000L);
        insertIndicator(104L, 1L, "0.0", 20240401L, 20240405L, 1_712_275_200_000L);
        insertIndicator(105L, 1L, "104.0", 20240501L, 20240505L, 1_714_867_200_000L);

        insertIndicator(201L, 2L, "3.5", 20240101L, 20240105L, 1_704_412_800_000L);
    }

    @Test
    @DisplayName("findByIndCodeIdAndObservationDate returns rows within range mapped to view")
    void seriesWithinRange() {
        List<IndicatorSeriesView> result = seriesAdapter.findByIndCodeIdAndDateRange(
                1L, 20240101L, 20240301L);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(IndicatorSeriesView::observationDate)
                .containsExactlyInAnyOrder(20240101L, 20240201L, 20240301L);
        assertThat(result).allMatch(v -> v.indCodeId() == 1L);
    }

    @Test
    @DisplayName("series query returns empty when no rows match")
    void seriesEmptyOutOfRange() {
        List<IndicatorSeriesView> result = seriesAdapter.findByIndCodeIdAndDateRange(
                1L, 20250101L, 20250201L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById returns the meta view for a known indicator code id")
    void metaFindById() {
        Optional<IndicatorMetaView> result = metaAdapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().indicatorCode()).isEqualTo("CPIAUCSL");
        assertThat(result.get().type()).isEqualTo("INFLATION");
        assertThat(result.get().frequency()).isEqualTo("monthly");
    }

    @Test
    @DisplayName("findById returns empty for unknown id")
    void metaFindByIdMissing() {
        assertThat(metaAdapter.findById(9999L)).isEmpty();
    }

    @Test
    @DisplayName("findAll returns every code row mapped")
    void metaFindAll() {
        List<IndicatorMetaView> all = metaAdapter.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(IndicatorMetaView::indicatorCode)
                .containsExactlyInAnyOrder("CPIAUCSL", "UNRATE", "GDP");
    }

    @Test
    @DisplayName("findByType filters by category/type column")
    void categoryFindByType() {
        List<IndicatorMetaView> inflation = categoryAdapter.findByType("INFLATION");
        List<IndicatorMetaView> employment = categoryAdapter.findByType("EMPLOYMENT");
        List<IndicatorMetaView> unknown = categoryAdapter.findByType("DOES_NOT_EXIST");

        assertThat(inflation).hasSize(1);
        assertThat(inflation.get(0).indicatorCode()).isEqualTo("CPIAUCSL");
        assertThat(employment).hasSize(1);
        assertThat(employment.get(0).indicatorCode()).isEqualTo("UNRATE");
        assertThat(unknown).isEmpty();
    }

    @Test
    @DisplayName("indicator_change_rate uses LAG window function and returns null on first row")
    void changeRateLagFirstRowIsNull() {
        List<IndicatorChangeRateView> rates = changeRateAdapter.findByIndCodeId(1L);

        assertThat(rates).hasSize(5);
        IndicatorChangeRateView first = rates.get(0);
        assertThat(first.observationDate()).isEqualTo(20240101L);
        assertThat(first.previousValue()).isNull();
        assertThat(first.changeRate()).isNull();
    }

    @Test
    @DisplayName("indicator_change_rate computes (value - previous) / previous on subsequent rows")
    void changeRateLagComputesDelta() {
        List<IndicatorChangeRateView> rates = changeRateAdapter.findByIndCodeId(1L);

        // 2nd row: value=101.5, prev=100.0 → 0.015
        IndicatorChangeRateView second = rates.get(1);
        assertThat(second.previousValue()).isEqualByComparingTo("100.0");
        assertThat(second.changeRate()).isNotNull();
        assertThat(second.changeRate().doubleValue()).isCloseTo(0.015, org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    @DisplayName("indicator_change_rate returns null change_rate when previous value is zero")
    void changeRateZeroDivisorYieldsNull() {
        List<IndicatorChangeRateView> rates = changeRateAdapter.findByIndCodeId(1L);

        // observationDate=20240501 has previous value=0.0 (from 20240401 row) → null
        IndicatorChangeRateView afterZero = rates.stream()
                .filter(r -> r.observationDate() == 20240501L)
                .findFirst().orElseThrow();
        assertThat(afterZero.previousValue()).isEqualByComparingTo("0.0");
        assertThat(afterZero.changeRate()).isNull();
    }

    private void insertCode(Long id, String code, String country, String type, String frequency, String unit) {
        jdbc.update(
                "INSERT INTO economic_indicator_code (id, indicator_code, country, type, frequency, unit) "
                        + "VALUES (:id, :code, :country, :type, :freq, :unit)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("code", code).addValue("country", country)
                        .addValue("type", type).addValue("freq", frequency).addValue("unit", unit)
        );
    }

    private void insertIndicator(Long id, Long indCodeId, String value,
                                 Long observationDate, Long releaseDate, Long timestamp) {
        jdbc.update(
                "INSERT INTO economic_indicator (id, ind_code_id, value, observation_date, release_date, timestamp) "
                        + "VALUES (:id, :icid, :val, :obs, :rel, :ts)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("icid", indCodeId)
                        .addValue("val", new BigDecimal(value))
                        .addValue("obs", observationDate).addValue("rel", releaseDate).addValue("ts", timestamp)
        );
    }
}
