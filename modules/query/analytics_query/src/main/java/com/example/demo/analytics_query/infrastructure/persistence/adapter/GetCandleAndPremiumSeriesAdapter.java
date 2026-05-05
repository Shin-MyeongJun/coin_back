package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.analytics_query.application.dto.CandleAndPremiumView;
import com.example.demo.analytics_query.application.port.out.GetCandleAndPremiumSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetCandleAndPremiumSeriesAdapter implements GetCandleAndPremiumSeriesPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<CandleAndPremiumView> find(Long marketCodeId, Long baseExchangeId, Long compareExchangeId, String interval, Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/candle_and_premium_series.sql");
        return jdbcTemplate.query(sql,
                Map.of("marketCodeId", marketCodeId, "baseExchangeId", baseExchangeId,
                       "compareExchangeId", compareExchangeId, "interval", interval,
                       "fromTs", fromTs, "toTs", toTs),
                new DataClassRowMapper<>(CandleAndPremiumView.class));
    }
}
