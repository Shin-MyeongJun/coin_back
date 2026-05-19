package com.example.demo.market_data_query.infrastructure.persistence.integration;

import com.example.demo.market_data_query.application.dto.PremiumRankingView;
import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;
import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetPremiumRankingAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetPremiumSnapshotByBaseAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetPremiumTimeSeriesAdapter;
import com.example.demo.infra_shard.paging.CursorDirection;
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

import static org.assertj.core.api.Assertions.assertThat;

@Import({
        GetPremiumSnapshotByBaseAdapter.class,
        GetPremiumRankingAdapter.class,
        GetPremiumTimeSeriesAdapter.class
})
class PremiumQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetPremiumSnapshotByBaseAdapter snapshotAdapter;

    @Autowired
    private GetPremiumRankingAdapter rankingAdapter;

    @Autowired
    private GetPremiumTimeSeriesAdapter timeSeriesAdapter;

    @BeforeEach
    void seed() {
        // premium 테이블은 market_data 모듈이 소유하므로 이 모듈 엔티티 스캔에 포함되지 않아
        // Hibernate ddl-auto 가 만들지 않는다. 테스트 시작마다 CREATE-IF-NOT-EXISTS 로 만들고 비운다.
        jdbc.getJdbcTemplate().execute(
                "CREATE TABLE IF NOT EXISTS premium ("
                        + " id bigint primary key,"
                        + " symbol varchar(64) not null,"
                        + " base_exchange_id bigint not null,"
                        + " compare_exchange_id bigint not null,"
                        + " timestamp bigint not null,"
                        + " bid numeric(30,10) not null,"
                        + " ask numeric(30,10) not null"
                        + ")"
        );
        jdbc.update("DELETE FROM premium", Map.of());

        insertPremium(1L, "BTC-KRW", 1L, 2L, 1_700_000_000_000L, "0.0250", "0.0260");
        insertPremium(2L, "BTC-KRW", 1L, 2L, 1_700_000_001_000L, "0.0300", "0.0310");
        insertPremium(3L, "BTC-KRW", 1L, 3L, 1_700_000_001_000L, "0.0150", "0.0160");
        insertPremium(4L, "ETH-KRW", 1L, 2L, 1_700_000_001_000L, "0.0400", "0.0410");
        insertPremium(5L, "SOL-KRW", 1L, 2L, 1_700_000_001_000L, "-0.0100", "-0.0090");
        insertPremium(6L, "XRP-KRW", 1L, 2L, 1_700_000_001_000L, "-0.0200", "-0.0190");
        insertPremium(7L, "BCH-USDT", 2L, 3L, 1_700_000_001_000L, "0.0050", "0.0060");
    }

    @Test
    @DisplayName("findByBase returns DISTINCT ON latest per (base_exchange_id, compare_exchange_id) where symbol LIKE base%")
    void snapshotByBaseReturnsLatestPerPair() {
        List<PremiumSnapshotView> result = snapshotAdapter.findByBase("BTC");

        assertThat(result).hasSize(2);
        // BTC-KRW base=1 compare=2: latest is id=2 ts=1700000001000
        PremiumSnapshotView a = result.stream()
                .filter(v -> v.compareExchangeId() == 2L).findFirst().orElseThrow();
        assertThat(a.timestamp()).isEqualTo(1_700_000_001_000L);
        assertThat(a.bid()).isEqualByComparingTo("0.0300");
        // BTC-KRW base=1 compare=3
        PremiumSnapshotView b = result.stream()
                .filter(v -> v.compareExchangeId() == 3L).findFirst().orElseThrow();
        assertThat(b.bid()).isEqualByComparingTo("0.0150");
    }

    @Test
    @DisplayName("findByBase returns empty when base does not match any symbol prefix")
    void snapshotByBaseEmpty() {
        assertThat(snapshotAdapter.findByBase("DOGE")).isEmpty();
    }

    @Test
    @DisplayName("findTopN ranking combines top positive and top negative bid groups")
    void rankingCombinesPositiveAndNegativeGroups() {
        List<PremiumRankingView> result = rankingAdapter.findTopN(10);

        // BTC-KRW(1,2)=0.030 ETH-KRW(1,2)=0.040 BTC-KRW(1,3)=0.015 BCH-USDT(2,3)=0.005 (positives)
        // SOL-KRW(1,2)=-0.010 XRP-KRW(1,2)=-0.020 (negatives)
        // Output ordered by bid DESC overall
        assertThat(result).extracting(PremiumRankingView::bid)
                .map(BigDecimal::doubleValue)
                .containsExactly(0.04, 0.03, 0.015, 0.005, -0.01, -0.02);

        // positive ranks 1..N descending by bid; negative ranks 1..N ascending by bid magnitude
        PremiumRankingView ethPositive = result.get(0);
        assertThat(ethPositive.symbol()).isEqualTo("ETH-KRW");
        assertThat(ethPositive.rank()).isEqualTo(1);

        // negatives ranked ASC: XRP(-0.020) is rank=1, SOL(-0.010) is rank=2
        PremiumRankingView xrpNegative = result.stream()
                .filter(v -> v.symbol().equals("XRP-KRW")).findFirst().orElseThrow();
        assertThat(xrpNegative.rank()).isEqualTo(1);
        PremiumRankingView solNegative = result.stream()
                .filter(v -> v.symbol().equals("SOL-KRW")).findFirst().orElseThrow();
        assertThat(solNegative.rank()).isEqualTo(2);
    }

    @Test
    @DisplayName("findTopN ranking respects the limit per positive/negative group")
    void rankingRespectsLimit() {
        List<PremiumRankingView> result = rankingAdapter.findTopN(2);

        // positives capped to 2 (ETH=0.040, BTC=0.030); negatives capped to 2 (SOL=-0.010, XRP=-0.020)
        assertThat(result).hasSize(4);
        assertThat(result).extracting(PremiumRankingView::bid)
                .map(BigDecimal::doubleValue)
                .containsExactly(0.04, 0.03, -0.01, -0.02);
    }

    @Test
    @Disabled("TimescaleDB time_bucket/last() required — skipped on plain postgres:16-alpine container")
    @DisplayName("findDownsampledCursor (forward) - TimescaleDB only")
    void seriesCursorForwardRequiresTimescaleDb() {
        List<PremiumTimeSeriesView> result = timeSeriesAdapter.findDownsampledCursor(
                1L, 2L, "BTC-KRW", 60, null, 10, CursorDirection.FORWARD);
        assertThat(result).isNotNull();
    }

    private void insertPremium(Long id, String symbol, Long baseEx, Long compareEx,
                               Long timestamp, String bid, String ask) {
        jdbc.update(
                "INSERT INTO premium (id, symbol, base_exchange_id, compare_exchange_id, timestamp, bid, ask) "
                        + "VALUES (:id, :sym, :base, :cmp, :ts, :bid, :ask)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sym", symbol)
                        .addValue("base", baseEx)
                        .addValue("cmp", compareEx)
                        .addValue("ts", timestamp)
                        .addValue("bid", new BigDecimal(bid))
                        .addValue("ask", new BigDecimal(ask))
        );
    }
}
