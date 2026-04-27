package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.read;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadEcoIndCodePort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.infra_shard.persistence.EntityToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EcoIndReadAdapter implements ReadEcoIndCodePort {

    private final JpaRepository<EcoIndCodeEntity, Long> repo;
    private final EntityToDomain<EcoIndCodeEntity, EconomicIndicatorCode> mapper;

    @Override
    public List<EconomicIndicatorCode> readAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }
}
