package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.save;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoIndCodeCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.infra_shard.persistence.EntityToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EcoCodeSaveAdapter implements FlushAndSaveEconomicValuePort<EconomicIndicatorCode> {

    private final JpaRepository<EcoIndCodeEntity, Long> repo;
    private final EcoIndCodeCache codeCache;
    private final DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> domainToEntity;
    private final EntityToDomain<EcoIndCodeEntity, EconomicIndicatorCode> entityToDomain;

    @Override
    public void saveAll(List<EconomicIndicatorCode> codes) {
        List<EcoIndCodeEntity> entities = codes.stream().map(domainToEntity::toEntity).toList();
        repo.saveAll(entities).forEach(saved ->
            codeCache.put(entityToDomain.toDomain(saved), saved.getId())
        );
        flush();
    }

    @Override
    public void flush() {
        repo.flush();
    }
}
