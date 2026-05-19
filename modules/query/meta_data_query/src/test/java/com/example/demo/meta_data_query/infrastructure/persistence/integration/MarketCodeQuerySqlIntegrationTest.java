package com.example.demo.meta_data_query.infrastructure.persistence.integration;

import com.example.demo.meta_data_query.application.dto.MarketCodeSearchResult;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.infrastructure.config.QueryDslConfig;
import com.example.demo.meta_data_query.infrastructure.persistence.adapter.GetMarketCodeListAdapter;
import com.example.demo.meta_data_query.infrastructure.persistence.adapter.GetMarketCodesByExchangeAdapter;
import com.example.demo.meta_data_query.infrastructure.persistence.adapter.SearchMarketCodeAdapter;
import com.example.demo.meta_data_query.infrastructure.persistence.mapper.MarketCodeViewMapper;
import com.example.demo.meta_data_query.infrastructure.persistence.querydsl.MarketCodeQueryDslRepository;
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

@Import({
        GetMarketCodeListAdapter.class,
        GetMarketCodesByExchangeAdapter.class,
        SearchMarketCodeAdapter.class,
        MarketCodeViewMapper.class,
        MarketCodeQueryDslRepository.class,
        QueryDslConfig.class
})
class MarketCodeQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetMarketCodeListAdapter listAdapter;

    @Autowired
    private GetMarketCodesByExchangeAdapter byExchangeAdapter;

    @Autowired
    private SearchMarketCodeAdapter searchAdapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM market_code", Map.of());
        insertMarketCode(101L, 1L, "BTC", "KRW", "KRW-BTC");
        insertMarketCode(102L, 1L, "ETH", "KRW", "KRW-ETH");
        insertMarketCode(201L, 2L, "BTC", "USDT", "BTCUSDT");
        insertMarketCode(202L, 2L, "ETH", "USDT", "ETHUSDT");
        insertMarketCode(203L, 2L, "SOL", "USDT", "SOLUSDT");
    }

    @Test
    @DisplayName("findAll returns every market_code row")
    void findAllReturnsAllRows() {
        List<MarketCodeView> all = listAdapter.findAll();

        assertThat(all).hasSize(5);
        assertThat(all).extracting(MarketCodeView::tradingPair)
                .containsExactlyInAnyOrder("KRW-BTC", "KRW-ETH", "BTCUSDT", "ETHUSDT", "SOLUSDT");
    }

    @Test
    @DisplayName("findByExchangeId filters by exchange_id")
    void findByExchangeIdFilters() {
        List<MarketCodeView> upbit = byExchangeAdapter.findByExchangeId(1L);
        List<MarketCodeView> binance = byExchangeAdapter.findByExchangeId(2L);

        assertThat(upbit).extracting(MarketCodeView::tradingPair)
                .containsExactlyInAnyOrder("KRW-BTC", "KRW-ETH");
        assertThat(binance).extracting(MarketCodeView::tradingPair)
                .containsExactlyInAnyOrder("BTCUSDT", "ETHUSDT", "SOLUSDT");
    }

    @Test
    @DisplayName("findByExchangeId returns empty when no rows match")
    void findByExchangeIdEmpty() {
        assertThat(byExchangeAdapter.findByExchangeId(999L)).isEmpty();
    }

    @Test
    @DisplayName("search by trading_pair substring uses case-insensitive containsIgnoreCase")
    void searchByTradingPairSubstring() {
        List<MarketCodeSearchResult> result = searchAdapter.search("usdt", null);

        assertThat(result).extracting(MarketCodeSearchResult::tradingPair)
                .containsExactlyInAnyOrder("BTCUSDT", "ETHUSDT", "SOLUSDT");
    }

    @Test
    @DisplayName("search by base symbol also matches via OR")
    void searchByBaseSymbol() {
        List<MarketCodeSearchResult> result = searchAdapter.search("sol", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).base()).isEqualTo("SOL");
        assertThat(result.get(0).tradingPair()).isEqualTo("SOLUSDT");
    }

    @Test
    @DisplayName("search narrows by exchangeId when supplied")
    void searchNarrowsByExchangeId() {
        List<MarketCodeSearchResult> result = searchAdapter.search("BTC", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).exchangeId()).isEqualTo(1L);
        assertThat(result.get(0).tradingPair()).isEqualTo("KRW-BTC");
    }

    @Test
    @DisplayName("search with blank keyword returns all rows in scope")
    void searchBlankKeywordReturnsAllInScope() {
        List<MarketCodeSearchResult> result = searchAdapter.search("", 2L);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(MarketCodeSearchResult::exchangeId)
                .containsOnly(2L);
    }

    private void insertMarketCode(Long id, Long exchangeId, String base, String quote, String pair) {
        jdbc.update(
                "INSERT INTO market_code (id, exchange_id, base, quote, trading_pair) "
                        + "VALUES (:id, :exchangeId, :base, :quote, :pair)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("exchangeId", exchangeId)
                        .addValue("base", base)
                        .addValue("quote", quote)
                        .addValue("pair", pair)
        );
    }
}
