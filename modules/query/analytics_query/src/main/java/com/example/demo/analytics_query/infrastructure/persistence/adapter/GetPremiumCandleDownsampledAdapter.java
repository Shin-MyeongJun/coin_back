package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetPremiumCandleDownsampledAdapter implements GetPremiumCandleDownsampledPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<PremiumCandleView> findDownsampled(String symbol, Long baseExchangeId, Long compareExchangeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/premium_candle_downsampled.sql");
        return jdbcTemplate.query(sql,
                Map.of("symbol", symbol, "baseExchangeId", baseExchangeId,
                       "compareExchangeId", compareExchangeId, "interval", sourceInterval,
                       "targetBucketSeconds", targetBucketSeconds, "fromTs", fromTs, "toTs", toTs),
                new DataClassRowMapper<>(PremiumCandleView.class));
    }
}
