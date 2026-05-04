package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.save;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EcoSaveAdapter implements FlushAndSaveEconomicValuePort<EconomicRawIndicator> {

    private final JpaRepository<EcoIndEntity, Long> repo;
    private final DomainToEntity<EconomicRawIndicator, EcoIndEntity> mapper;

    @Override
    public void saveAll(List<EconomicRawIndicator> domains) {
        List<EcoIndEntity> entities = domains.stream().map(mapper::toEntity).toList();
        repo.saveAll(entities);
        flush();
    }

    @Override
    public void flush() {
        repo.flush();
    }

    @Override
    public void save(EconomicRawIndicator economicRawIndicator) {
        repo.save(mapper.toEntity(economicRawIndicator));
    }
}
