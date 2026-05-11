package com.example.demo.meta_data_query.infrastructure.persistence;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.meta_data_query.application.dto.MappingIntegrityResult;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.infrastructure.persistence.adapter.CheckMappingIntegrityAdapter;
import com.example.demo.meta_data_query.infrastructure.persistence.entity.MarketCodeQueryEntity;
import com.example.demo.meta_data_query.infrastructure.persistence.mapper.MarketCodeViewMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class MetaDataQueryPersistenceTest {

    @Test
    void marketCodeMapperCopiesEntityFieldsToView() {
        MarketCodeQueryEntity entity = new MarketCodeQueryEntity();
        set(entity, "id", 100L);
        set(entity, "exchangeId", 1L);
        set(entity, "base", "BTC");
        set(entity, "quote", "KRW");
        set(entity, "tradingPair", "KRW-BTC");

        MarketCodeView view = new MarketCodeViewMapper().toDomain(entity);

        assertThat(view).isEqualTo(new MarketCodeView(100L, 1L, "BTC", "KRW", "KRW-BTC"));
    }

    @Test
    void mappingIntegrityAdapterLoadsSqlWithoutParameters() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        SqlLoader sqlLoader = mock(SqlLoader.class);
        CheckMappingIntegrityAdapter adapter = new CheckMappingIntegrityAdapter(jdbcTemplate, sqlLoader);
        String sql = "select mapping integrity";
        List<MappingIntegrityResult> expected = List.of(new MappingIntegrityResult(
                1L,
                "UPBIT",
                100L,
                "KRW-BTC",
                "OK"
        ));

        when(sqlLoader.load("sql/check_mapping_integrity.sql")).thenReturn(sql);
        when(jdbcTemplate.query(eq(sql), any(Map.class), any(DataClassRowMapper.class))).thenReturn(expected);

        List<MappingIntegrityResult> actual = adapter.check();

        assertThat(actual).isSameAs(expected);

        ArgumentCaptor<Map<String, Object>> params = mapCaptor();
        verify(jdbcTemplate).query(eq(sql), params.capture(), any(DataClassRowMapper.class));
        assertThat(params.getValue()).isEmpty();
    }

    private static void set(Object target, String name, Object value) {
        ReflectionTestUtils.setField(target, name, value);
    }

    private static ArgumentCaptor<Map<String, Object>> mapCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }
}
