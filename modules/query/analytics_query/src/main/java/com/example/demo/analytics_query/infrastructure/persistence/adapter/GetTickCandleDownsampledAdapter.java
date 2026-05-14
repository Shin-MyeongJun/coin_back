package com.example.demo.analytics_query.infrastructure.persistence.adapter;

import com.example.demo.analytics_query.application.dto.TickCandleView;
import com.example.demo.analytics_query.application.port.out.GetTickCandleDownsampledPort;
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
public class GetTickCandleDownsampledAdapter implements GetTickCandleDownsampledPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<TickCandleView> findDownsampled(Long marketCodeId, String sourceInterval,
                                                int targetBucketSeconds, Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/candle_downsampled.sql");
        return jdbcTemplate.query(sql,
                Map.of("marketCodeId", marketCodeId, "interval", sourceInterval,
                       "targetBucketSeconds", targetBucketSeconds, "fromTs", fromTs, "toTs", toTs),
                new DataClassRowMapper<>(TickCandleView.class));
    }

    @Override
    public List<TickCandleView> findDownsampledCursor(Long marketCodeId, String sourceInterval, int targetBucketSeconds,
                                                      Long cursor, int limit, CursorDirection direction) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("marketCodeId", marketCodeId)
                .addValue("interval", sourceInterval)
                .addValue("targetBucketSeconds", targetBucketSeconds)
                .addValue("cursor", cursor, Types.BIGINT)
                .addValue("limit", limit);

        return switch (direction) {
            case BACKWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_candle_downsampled_backward.sql");
                List<TickCandleView> desc = new ArrayList<>(jdbcTemplate.query(
                        sql, params, new DataClassRowMapper<>(TickCandleView.class)));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_candle_downsampled_forward.sql");
                yield jdbcTemplate.query(sql, params, new DataClassRowMapper<>(TickCandleView.class));
            }
        };
    }
}
