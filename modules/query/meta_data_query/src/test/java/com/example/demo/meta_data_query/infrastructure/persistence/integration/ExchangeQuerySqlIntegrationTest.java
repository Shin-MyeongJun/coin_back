package com.example.demo.meta_data_query.infrastructure.persistence.integration;

import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.infrastructure.persistence.adapter.GetExchangeListAdapter;
import com.example.demo.meta_data_query.infrastructure.persistence.mapper.ExchangeViewMapper;
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

@Import({GetExchangeListAdapter.class, ExchangeViewMapper.class})
class ExchangeQuerySqlIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private GetExchangeListAdapter adapter;

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM exchange", Map.of());
        insertExchange(1L, "UPBIT", "SPOT", "KRW", "KR", "ACTIVE");
        insertExchange(2L, "BINANCE", "SPOT", "USDT", "GLOBAL", "ACTIVE");
        insertExchange(3L, "BINANCE_FUTURES", "FUTURES", "USDT", "GLOBAL", "PAUSED");
    }

    @Test
    @DisplayName("findAll returns every exchange row mapped to ExchangeView")
    void findAllReturnsAllExchanges() {
        List<ExchangeView> result = adapter.findAll();

        assertThat(result)
                .extracting(ExchangeView::id, ExchangeView::name, ExchangeView::exchangeType,
                        ExchangeView::quote, ExchangeView::country, ExchangeView::status)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(1L, "UPBIT", "SPOT", "KRW", "KR", "ACTIVE"),
                        org.assertj.core.groups.Tuple.tuple(2L, "BINANCE", "SPOT", "USDT", "GLOBAL", "ACTIVE"),
                        org.assertj.core.groups.Tuple.tuple(3L, "BINANCE_FUTURES", "FUTURES", "USDT", "GLOBAL", "PAUSED")
                );
    }

    @Test
    @DisplayName("findAll returns empty list when table is empty")
    void findAllEmpty() {
        jdbc.update("DELETE FROM exchange", Map.of());

        assertThat(adapter.findAll()).isEmpty();
    }

    private void insertExchange(Long id, String name, String type, String quote, String country, String status) {
        jdbc.update(
                "INSERT INTO exchange (id, name, exchange_type, quote, country, status) "
                        + "VALUES (:id, :name, :type, :quote, :country, :status)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("type", type)
                        .addValue("quote", quote)
                        .addValue("country", country)
                        .addValue("status", status)
        );
    }
}
