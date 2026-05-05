package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.analytics_query.application.dto.TickIndicatorView;
import com.example.demo.analytics_query.application.port.out.GetLatestIndicatorMultiMarketPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetLatestIndicatorMultiMarketAdapter implements GetLatestIndicatorMultiMarketPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<TickIndicatorView> findLatestForMarkets(List<Long> marketCodeIds, String interval, String type) {
        String sql = sqlLoader.load("sql/latest_indicator_multi_market.sql");
        return jdbcTemplate.query(sql,
                Map.of("marketCodeIds", marketCodeIds.toArray(new Long[0]), "interval", interval, "type", type),
                new DataClassRowMapper<>(TickIndicatorView.class));
    }
}
