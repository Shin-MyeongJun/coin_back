package com.example.demo.economic_query.infrastructure.persistence;

import com.example.demo.economic_query.application.dto.IndicatorChangeRateView;
import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.dto.IndicatorSeriesView;
import com.example.demo.economic_query.infrastructure.persistence.adapter.GetIndicatorChangeRateAdapter;
import com.example.demo.economic_query.infrastructure.persistence.entity.EcoIndCodeQueryEntity;
import com.example.demo.economic_query.infrastructure.persistence.entity.EcoIndQueryEntity;
import com.example.demo.economic_query.infrastructure.persistence.mapper.IndicatorViewMapper;
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
class EconomicQueryPersistenceTest {

    @Test
    void indicatorMapperCopiesSeriesAndMetaFieldsToViews() {
        IndicatorViewMapper mapper = new IndicatorViewMapper();
        EcoIndQueryEntity seriesEntity = new EcoIndQueryEntity();
        set(seriesEntity, "indCodeId", 10L);
        set(seriesEntity, "value", new BigDecimal("3.250000"));
        set(seriesEntity, "observationDate", 20260511L);
        set(seriesEntity, "releaseDate", 20260512L);
        set(seriesEntity, "timestamp", 1_700_000_000L);

        EcoIndCodeQueryEntity metaEntity = new EcoIndCodeQueryEntity();
        set(metaEntity, "id", 10L);
        set(metaEntity, "indicatorCode", "CPI");
        set(metaEntity, "country", "US");
        set(metaEntity, "type", "INFLATION");
        set(metaEntity, "frequency", "MONTHLY");
        set(metaEntity, "unit", "INDEX");

        IndicatorSeriesView seriesView = mapper.toSeriesView(seriesEntity);
        IndicatorMetaView metaView = mapper.toMetaView(metaEntity);

        assertThat(seriesView).isEqualTo(new IndicatorSeriesView(
                10L,
                new BigDecimal("3.250000"),
                20260511L,
                20260512L,
                1_700_000_000L
        ));
        assertThat(metaView).isEqualTo(new IndicatorMetaView(
                10L,
                "CPI",
                "US",
                "INFLATION",
                "MONTHLY",
                "INDEX"
        ));
    }

    @Test
    void indicatorChangeRateAdapterLoadsSqlAndBindsIndicatorCodeId() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        SqlLoader sqlLoader = mock(SqlLoader.class);
        GetIndicatorChangeRateAdapter adapter = new GetIndicatorChangeRateAdapter(jdbcTemplate, sqlLoader);
        String sql = "select indicator change rate";
        List<IndicatorChangeRateView> expected = List.of(new IndicatorChangeRateView(
                20260511L,
                new BigDecimal("3.250000"),
                new BigDecimal("3.100000"),
                new BigDecimal("4.838710")
        ));

        when(sqlLoader.load("sql/indicator_change_rate.sql")).thenReturn(sql);
        when(jdbcTemplate.query(eq(sql), any(Map.class), any(DataClassRowMapper.class))).thenReturn(expected);

        List<IndicatorChangeRateView> actual = adapter.findByIndCodeId(10L);

        assertThat(actual).isSameAs(expected);

        ArgumentCaptor<Map<String, Object>> params = mapCaptor();
        verify(jdbcTemplate).query(eq(sql), params.capture(), any(DataClassRowMapper.class));
        assertThat(params.getValue()).containsEntry("indCodeId", 10L);
    }

    private static void set(Object target, String name, Object value) {
        ReflectionTestUtils.setField(target, name, value);
    }

    private static ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
