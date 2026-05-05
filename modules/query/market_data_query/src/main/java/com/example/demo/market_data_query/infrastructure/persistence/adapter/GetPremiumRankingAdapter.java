package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.PremiumRankingView;
import com.example.demo.market_data_query.application.port.out.GetPremiumRankingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetPremiumRankingAdapter implements GetPremiumRankingPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<PremiumRankingView> findTopN(int limit) {
        String sql = sqlLoader.load("sql/range/premium_ranking.sql");
        return jdbcTemplate.query(sql,
                Map.of("limit", limit),
                new DataClassRowMapper<>(PremiumRankingView.class));
    }
}
