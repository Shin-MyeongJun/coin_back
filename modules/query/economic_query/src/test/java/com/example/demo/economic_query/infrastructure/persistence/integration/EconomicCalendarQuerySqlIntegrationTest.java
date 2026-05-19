package com.example.demo.economic_query.infrastructure.persistence.integration;

import com.example.demo.economic_query.application.dto.EconomicCalendarView;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetEconomicCalendarAdapter;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Import({GetEconomicCalendarAdapter.class, IndicatorViewMapper.class})
class EconomicCalendarQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetEconomicCalendarAdapter adapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM economic_schedule", Map.of());
        insertSchedule(1L, 1L, "CPI-202401", 20240105L, "RELEASED", 1_704_412_800_000L);
        insertSchedule(2L, 1L, "CPI-202402", 20240205L, "RELEASED", 1_707_091_200_000L);
        insertSchedule(3L, 2L, "UNRATE-202402", 20240210L, "SCHEDULED", null);
        insertSchedule(4L, 2L, "UNRATE-202403", 20240305L, "SCHEDULED", null);
        insertSchedule(5L, 3L, "GDP-2024Q1", 20240430L, "SCHEDULED", null);
    }

    @Test
    @DisplayName("findByReleaseDateBetween returns rows whose release_date is within inclusive range")
    void findByReleaseDateBetweenInclusive() {
        List<EconomicCalendarView> rows = adapter.findByReleaseDateBetween(20240201L, 20240301L);

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(EconomicCalendarView::releaseCode)
                .containsExactlyInAnyOrder("CPI-202402", "UNRATE-202402");
    }

    @Test
    @DisplayName("findByReleaseDateBetween returns empty list when no rows match")
    void findByReleaseDateBetweenEmpty() {
        assertThat(adapter.findByReleaseDateBetween(20250101L, 20250201L)).isEmpty();
    }

    @Test
    @DisplayName("findByReleaseDateBetween returns all rows when range covers all schedule rows")
    void findByReleaseDateBetweenAll() {
        List<EconomicCalendarView> rows = adapter.findByReleaseDateBetween(0L, Long.MAX_VALUE);

        assertThat(rows).hasSize(5);
        assertThat(rows).extracting(EconomicCalendarView::status)
                .contains("RELEASED", "SCHEDULED");
    }

    @Test
    @DisplayName("calendar view preserves status and fetchedAt fields")
    void calendarViewFields() {
        List<EconomicCalendarView> released = adapter.findByReleaseDateBetween(20240101L, 20240131L);

        assertThat(released).hasSize(1);
        EconomicCalendarView row = released.get(0);
        assertThat(row.indCodeId()).isEqualTo(1L);
        assertThat(row.status()).isEqualTo("RELEASED");
        assertThat(row.fetchedAt()).isEqualTo(1_704_412_800_000L);
    }

    @Test
    @DisplayName("scheduled rows expose null fetchedAt")
    void scheduledRowsHaveNullFetchedAt() {
        List<EconomicCalendarView> rows = adapter.findByReleaseDateBetween(20240201L, 20240301L);

        EconomicCalendarView unrate = rows.stream()
                .filter(r -> r.releaseCode().equals("UNRATE-202402"))
                .findFirst().orElseThrow();
        assertThat(unrate.status()).isEqualTo("SCHEDULED");
        assertThat(unrate.fetchedAt()).isNull();
    }

    private void insertSchedule(Long id, Long indCodeId, String releaseCode,
                                Long releaseDate, String status, Long fetchedAt) {
        jdbc.update(
                "INSERT INTO economic_schedule (id, ind_code_id, release_code, release_date, status, fetched_at) "
                        + "VALUES (:id, :icid, :code, :date, :status, :fetched)",
                new MapSqlParameterSource()
                        .addValue("id", id).addValue("icid", indCodeId).addValue("code", releaseCode)
                        .addValue("date", releaseDate).addValue("status", status).addValue("fetched", fetchedAt)
        );
    }
}
