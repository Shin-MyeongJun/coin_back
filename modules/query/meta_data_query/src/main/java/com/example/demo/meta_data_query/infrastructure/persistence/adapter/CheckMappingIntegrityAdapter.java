package com.example.demo.meta_data_query.infrastructure.persistence.adapter;

import com.example.demo.infra_shard.sql.SqlLoader;
import com.example.demo.meta_data_query.application.dto.MappingIntegrityResult;
import com.example.demo.meta_data_query.application.port.out.CheckMappingIntegrityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CheckMappingIntegrityAdapter implements CheckMappingIntegrityPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlLoader sqlLoader;

    @Override
    public List<MappingIntegrityResult> check() {
        String sql = sqlLoader.load("sql/check_mapping_integrity.sql");
        return jdbcTemplate.query(sql, Map.of(), new DataClassRowMapper<>(MappingIntegrityResult.class));
    }
}
