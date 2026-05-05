package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.application.port.out.GetPremiumTimeSeriesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetPremiumTimeSeriesAdapter implements GetPremiumTimeSeriesPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<PremiumTimeSeriesView> findDownsampled(Long baseExchangeId, Long compareExchangeId,
                                                       String symbol, int bucketSeconds,
                                                       Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/range/range_premium_downsampled.sql");
        return jdbcTemplate.query(sql,
                Map.of("baseExchangeId", baseExchangeId,
                       "compareExchangeId", compareExchangeId,
                       "symbol", symbol,
                       "bucketSeconds", bucketSeconds,
                       "fromTs", fromTs,
                       "toTs", toTs),
                new DataClassRowMapper<>(PremiumTimeSeriesView.class));
    }
}
