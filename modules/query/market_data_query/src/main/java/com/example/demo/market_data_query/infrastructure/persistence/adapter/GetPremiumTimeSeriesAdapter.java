package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.application.port.out.GetPremiumTimeSeriesPort;
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

    @Override
    public List<PremiumTimeSeriesView> findDownsampledCursor(Long baseExchangeId, Long compareExchangeId,
                                                             String symbol, int bucketSeconds,
                                                             Long cursor, int limit,
                                                             CursorDirection direction) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("baseExchangeId", baseExchangeId)
                .addValue("compareExchangeId", compareExchangeId)
                .addValue("symbol", symbol)
                .addValue("bucketSeconds", bucketSeconds)
                .addValue("cursor", cursor, Types.BIGINT)
                .addValue("limit", limit);

        return switch (direction) {
            case BACKWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_premium_downsampled_backward.sql");
                List<PremiumTimeSeriesView> desc = new ArrayList<>(jdbcTemplate.query(
                        sql, params, new DataClassRowMapper<>(PremiumTimeSeriesView.class)));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_premium_downsampled_forward.sql");
                yield jdbcTemplate.query(sql, params, new DataClassRowMapper<>(PremiumTimeSeriesView.class));
            }
        };
    }
}
