package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.port.out.GetLatestTickBulkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetLatestTickBulkAdapter implements GetLatestTickBulkPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<TickBulkView> findByMarketCodeIds(List<Long> marketCodeIds) {
        String sql = sqlLoader.load("sql/latest/latest_tick_bulk.sql");
        return jdbcTemplate.query(sql,
                Map.of("marketCodeIds", marketCodeIds.toArray(new Long[0])),
                new DataClassRowMapper<>(TickBulkView.class));
    }
}
