package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.PremiumCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumCandleDownsampledPort;
import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.sql.SqlLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetPremiumCandleDownsampledAdapter implements GetPremiumCandleDownsampledPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<PremiumCandleView> findDownsampled(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                   String sourceInterval, int targetBucketSeconds,
                                                   Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/premium_candle_downsampled.sql");
        return jdbcTemplate.query(sql,
                Map.of("symbol", symbol, "baseExchangeId", baseExchangeId,
                       "compareExchangeId", compareExchangeId, "interval", sourceInterval,
                       "targetBucketSeconds", targetBucketSeconds, "fromTs", fromTs, "toTs", toTs),
                new DataClassRowMapper<>(PremiumCandleView.class));
    }

    @Override
    public List<PremiumCandleView> findDownsampledCursor(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                        String sourceInterval, int targetBucketSeconds,
                                                        Long cursor, int limit, CursorDirection direction) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("symbol", symbol)
                .addValue("baseExchangeId", baseExchangeId)
                .addValue("compareExchangeId", compareExchangeId)
                .addValue("interval", sourceInterval)
                .addValue("targetBucketSeconds", targetBucketSeconds)
                .addValue("cursor", cursor, Types.BIGINT)
                .addValue("limit", limit);

        return switch (direction) {
            case BACKWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_premium_candle_downsampled_backward.sql");
                List<PremiumCandleView> desc = new ArrayList<>(jdbcTemplate.query(
                        sql, params, new DataClassRowMapper<>(PremiumCandleView.class)));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_premium_candle_downsampled_forward.sql");
                yield jdbcTemplate.query(sql, params, new DataClassRowMapper<>(PremiumCandleView.class));
            }
        };
    }
}
