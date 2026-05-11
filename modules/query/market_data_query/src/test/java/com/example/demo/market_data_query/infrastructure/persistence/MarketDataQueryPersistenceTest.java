package com.example.demo.market_data_query.infrastructure.persistence;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.infrastructure.persistence.adapter.GetLatestTickBulkAdapter;
import com.example.demo.market_data_query.infrastructure.persistence.entity.TickQueryEntity;
import com.example.demo.market_data_query.infrastructure.persistence.mapper.TickViewMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class MarketDataQueryPersistenceTest {

    @Test
    void tickMapperCopiesEntityFieldsToLatestView() {
        TickQueryEntity entity = new TickQueryEntity();
        set(entity, "id", 1L);
        set(entity, "marketCodeId", 10L);
        set(entity, "timestamp", 1_700_000_000L);
        set(entity, "bid", new BigDecimal("70000.12"));
        set(entity, "ask", new BigDecimal("70001.34"));

        TickLatestView view = new TickViewMapper().toLatestView(entity);

        assertThat(view).isEqualTo(new TickLatestView(
                1L,
                10L,
                1_700_000_000L,
                new BigDecimal("70000.12"),
                new BigDecimal("70001.34")
        ));
    }

    @Test
    void latestTickBulkAdapterLoadsSqlAndBindsMarketCodeIds() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        SqlLoader sqlLoader = mock(SqlLoader.class);
        GetLatestTickBulkAdapter adapter = new GetLatestTickBulkAdapter(jdbcTemplate, sqlLoader);
        String sql = "select latest ticks";
        List<TickBulkView> expected = List.of(new TickBulkView(
                10L,
                1_700_000_000L,
                new BigDecimal("70000.12"),
                new BigDecimal("70001.34")
        ));

        when(sqlLoader.load("sql/latest/latest_tick_bulk.sql")).thenReturn(sql);
        when(jdbcTemplate.query(eq(sql), any(Map.class), any(DataClassRowMapper.class))).thenReturn(expected);

        List<TickBulkView> actual = adapter.findByMarketCodeIds(List.of(10L, 20L));

        assertThat(actual).isSameAs(expected);

        ArgumentCaptor<Map<String, Object>> params = mapCaptor();
        verify(jdbcTemplate).query(eq(sql), params.capture(), any(DataClassRowMapper.class));
        assertThat((Long[]) params.getValue().get("marketCodeIds")).containsExactly(10L, 20L);
    }

    private static void set(Object target, String name, Object value) {
        ReflectionTestUtils.setField(target, name, value);
    }

    private static ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
