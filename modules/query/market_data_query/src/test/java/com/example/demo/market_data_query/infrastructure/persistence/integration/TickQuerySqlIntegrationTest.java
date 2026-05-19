package com.example.demo.market_data_query.infrastructure.persistence.integration;

import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetLatestTickAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetLatestTickBulkAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.TickViewMapper;
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

@Import({GetLatestTickAdapter.class, GetLatestTickBulkAdapter.class, TickViewMapper.class})
class TickQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetLatestTickAdapter latestAdapter;

    @Autowired
    private GetLatestTickBulkAdapter bulkAdapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM tick", Map.of());
        insertTick(1L, 10L, 1_700_000_000_000L, "70000.0", "70010.0");
        insertTick(2L, 10L, 1_700_000_001_000L, "70100.0", "70110.0");
        insertTick(3L, 10L, 1_700_000_002_000L, "70200.0", "70210.0");
        insertTick(4L, 20L, 1_700_000_000_500L, "2000.0", "2001.0");
        insertTick(5L, 20L, 1_700_000_003_000L, "2050.0", "2051.0");
        insertTick(6L, 30L, 1_700_000_000_000L, "0.5", "0.6");
    }

    @Test
    @DisplayName("findByMarketCodeId returns top-1 row sorted by timestamp desc")
    void findLatestReturnsTopByTimestampDesc() {
        Optional<TickLatestView> latest = latestAdapter.findByMarketCodeId(10L);

        assertThat(latest).isPresent();
        assertThat(latest.get().timestamp()).isEqualTo(1_700_000_002_000L);
        assertThat(latest.get().bid()).isEqualByComparingTo("70200.0");
        assertThat(latest.get().ask()).isEqualByComparingTo("70210.0");
    }

    @Test
    @DisplayName("findByMarketCodeId returns empty Optional for unknown market_code_id")
    void findLatestReturnsEmptyForMissing() {
        assertThat(latestAdapter.findByMarketCodeId(9999L)).isEmpty();
    }

    @Test
    @DisplayName("findByMarketCodeIds bulk uses DISTINCT ON to return one row per market_code_id")
    void findBulkReturnsOneRowPerMarketCode() {
        List<TickBulkView> result = bulkAdapter.findByMarketCodeIds(List.of(10L, 20L, 30L));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(TickBulkView::marketCodeId)
                .containsExactlyInAnyOrder(10L, 20L, 30L);

        TickBulkView ten = result.stream().filter(v -> v.marketCodeId() == 10L).findFirst().orElseThrow();
        assertThat(ten.timestamp()).isEqualTo(1_700_000_002_000L);
        assertThat(ten.bid()).isEqualByComparingTo("70200.0");

        TickBulkView twenty = result.stream().filter(v -> v.marketCodeId() == 20L).findFirst().orElseThrow();
        assertThat(twenty.timestamp()).isEqualTo(1_700_000_003_000L);
        assertThat(twenty.bid()).isEqualByComparingTo("2050.0");
    }

    @Test
    @DisplayName("findByMarketCodeIds filters out unknown market_code_ids")
    void findBulkFiltersUnknownIds() {
        List<TickBulkView> result = bulkAdapter.findByMarketCodeIds(List.of(10L, 9999L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).marketCodeId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("DISTINCT ON tie-break uses id DESC when timestamps collide")
    void distinctOnTieBreaksOnIdDesc() {
        insertTick(7L, 10L, 1_700_000_002_000L, "70999.0", "70999.5");

        List<TickBulkView> result = bulkAdapter.findByMarketCodeIds(List.of(10L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).bid()).isEqualByComparingTo("70999.0");
        assertThat(result.get(0).ask()).isEqualByComparingTo("70999.5");
    }

    @Test
    @DisplayName("latest adapter respects market_code_id isolation across multiple codes")
    void latestPerMarketCodeIsolatesData() {
        Optional<TickLatestView> ten = latestAdapter.findByMarketCodeId(10L);
        Optional<TickLatestView> twenty = latestAdapter.findByMarketCodeId(20L);
        Optional<TickLatestView> thirty = latestAdapter.findByMarketCodeId(30L);

        assertThat(ten).isPresent();
        assertThat(twenty).isPresent();
        assertThat(thirty).isPresent();
        assertThat(ten.get().marketCodeId()).isEqualTo(10L);
        assertThat(twenty.get().marketCodeId()).isEqualTo(20L);
        assertThat(thirty.get().marketCodeId()).isEqualTo(30L);
        assertThat(thirty.get().bid()).isEqualByComparingTo("0.5");
    }

    private void insertTick(Long id, Long marketCodeId, Long timestamp, String bid, String ask) {
        jdbc.update(
                "INSERT INTO tick (id, market_code_id, timestamp, bid, ask) "
                        + "VALUES (:id, :mc, :ts, :bid, :ask)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("mc", marketCodeId)
                        .addValue("ts", timestamp)
                        .addValue("bid", new BigDecimal(bid))
                        .addValue("ask", new BigDecimal(ask))
        );
    }
}
