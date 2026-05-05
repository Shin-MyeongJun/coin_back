package com.example.demo.economic_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.economic_query.application.dto.IndicatorChangeRateView;
import com.example.demo.economic_query.application.port.out.GetIndicatorChangeRatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetIndicatorChangeRateAdapter implements GetIndicatorChangeRatePort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<IndicatorChangeRateView> findByIndCodeId(Long indCodeId) {
        String sql = sqlLoader.load("sql/indicator_change_rate.sql");
        return jdbcTemplate.query(sql, Map.of("indCodeId", indCodeId),
                new DataClassRowMapper<>(IndicatorChangeRateView.class));
    }
}
