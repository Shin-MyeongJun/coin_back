package com.example.demo.analytics_query.infrastructure.persistence;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.infrastructure.persistence.adapter.GetTickCandleDownsampledAdapter;
import com.example.demo.analytics_query.infrastructure.persistence.entity.TickCandleQueryEntity;
import com.example.demo.analytics_query.infrastructure.persistence.mapper.TickCandleViewMapper;
import com.example.demo.infra_shard.sql.SqlLoader;
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
class AnalyticsQueryPersistenceTest {

    @Test
    void tickCandleMapperCopiesEntityFieldsToView() {
        TickCandleQueryEntity entity = new TickCandleQueryEntity();
        set(entity, "marketCodeId", 10L);
        set(entity, "interval", "1m");
        set(entity, "open", new BigDecimal("70000.00"));
        set(entity, "high", new BigDecimal("70100.00"));
        set(entity, "low", new BigDecimal("69900.00"));
        set(entity, "close", new BigDecimal("70050.00"));
        set(entity, "bucketOpenTs", 1_700_000_000L);
        set(entity, "bucketCloseTs", 1_700_000_059L);
        set(entity, "observeOpenTs", 1_700_000_001L);
        set(entity, "observeCloseTs", 1_700_000_058L);

        TickCandleView view = new TickCandleViewMapper().toView(entity);

        assertThat(view).isEqualTo(new TickCandleView(
                10L,
                "1m",
                new BigDecimal("70000.00"),
                new BigDecimal("70100.00"),
                new BigDecimal("69900.00"),
                new BigDecimal("70050.00"),
                1_700_000_000L,
                1_700_000_059L,
                1_700_000_001L,
                1_700_000_058L
        ));
    }

    @Test
    void tickCandleDownsampledAdapterLoadsSqlAndBindsRange() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        SqlLoader sqlLoader = mock(SqlLoader.class);
        GetTickCandleDownsampledAdapter adapter = new GetTickCandleDownsampledAdapter(jdbcTemplate, sqlLoader);
        String sql = "select candle downsampled";
        List<TickCandleView> expected = List.of(new TickCandleView(
                10L,
                "1m",
                new BigDecimal("70000.00"),
                new BigDecimal("70100.00"),
                new BigDecimal("69900.00"),
                new BigDecimal("70050.00"),
                1_700_000_000L,
                1_700_000_059L,
                1_700_000_001L,
                1_700_000_058L
        ));

        when(sqlLoader.load("sql/candle_downsampled.sql")).thenReturn(sql);
        when(jdbcTemplate.query(eq(sql), any(Map.class), any(DataClassRowMapper.class))).thenReturn(expected);

        List<TickCandleView> actual = adapter.findDownsampled(10L, "1m", 300, 1_700_000_000L, 1_700_003_600L);

        assertThat(actual).isSameAs(expected);

        ArgumentCaptor<Map<String, Object>> params = mapCaptor();
        verify(jdbcTemplate).query(eq(sql), params.capture(), any(DataClassRowMapper.class));
        assertThat(params.getValue()).containsEntry("marketCodeId", 10L)
                .containsEntry("interval", "1m")
                .containsEntry("targetBucketSeconds", 300)
                .containsEntry("fromTs", 1_700_000_000L)
                .containsEntry("toTs", 1_700_003_600L);
    }

    private static void set(Object target, String name, Object value) {
        ReflectionTestUtils.setField(target, name, value);
    }

    private static ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
