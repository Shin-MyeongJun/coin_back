package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.analytics_query.application.dto.CandleView;
import com.example.demo.analytics_query.application.port.out.GetCandleDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetCandleDownsampledAdapter implements GetCandleDownsampledPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<CandleView> findDownsampledTickCandles(Long marketCodeId, String sourceInterval, int targetBucketSeconds, Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/candle_downsampled.sql");
        return jdbcTemplate.query(sql,
                Map.of("marketCodeId", marketCodeId, "interval", sourceInterval,
                       "targetBucketSeconds", targetBucketSeconds, "fromTs", fromTs, "toTs", toTs),
                new DataClassRowMapper<>(CandleView.class));
    }
}
