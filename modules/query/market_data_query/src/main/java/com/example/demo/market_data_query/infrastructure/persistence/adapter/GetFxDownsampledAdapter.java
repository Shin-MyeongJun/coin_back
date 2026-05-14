package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxDownsampledPort;
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
public class GetFxDownsampledAdapter implements GetFxDownsampledPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<FxView> findDownsampled(String baseCurrency, String quoteCurrency,
                                        int bucketSeconds, Long fromTs, Long toTs) {
        String sql = sqlLoader.load("sql/range/range_fx_downsampled.sql");
        return jdbcTemplate.query(sql,
                Map.of("baseCurrency", baseCurrency,
                       "quoteCurrency", quoteCurrency,
                       "bucketSeconds", bucketSeconds,
                       "fromTs", fromTs,
                       "toTs", toTs),
                new DataClassRowMapper<>(FxView.class));
    }

    @Override
    public List<FxView> findDownsampledCursor(String baseCurrency, String quoteCurrency, int bucketSeconds,
                                              Long cursor, int limit, CursorDirection direction) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("baseCurrency", baseCurrency)
                .addValue("quoteCurrency", quoteCurrency)
                .addValue("bucketSeconds", bucketSeconds)
                .addValue("cursor", cursor, Types.BIGINT)
                .addValue("limit", limit);

        return switch (direction) {
            case BACKWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_fx_downsampled_backward.sql");
                List<FxView> desc = new ArrayList<>(jdbcTemplate.query(
                        sql, params, new DataClassRowMapper<>(FxView.class)));
                Collections.reverse(desc);
                yield desc;
            }
            case FORWARD -> {
                String sql = sqlLoader.load("sql/cursor/cursor_fx_downsampled_forward.sql");
                yield jdbcTemplate.query(sql, params, new DataClassRowMapper<>(FxView.class));
            }
        };
    }
}
