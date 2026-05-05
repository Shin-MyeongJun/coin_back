package com.example.demo.market_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;
import com.example.demo.market_data_query.application.port.out.GetPremiumSnapshotByBasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetPremiumSnapshotByBaseAdapter implements GetPremiumSnapshotByBasePort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<PremiumSnapshotView> findByBase(String base) {
        String sql = sqlLoader.load("sql/latest/latest_premium_by_base.sql");
        return jdbcTemplate.query(sql,
                Map.of("base", base),
                new DataClassRowMapper<>(PremiumSnapshotView.class));
    }
}
