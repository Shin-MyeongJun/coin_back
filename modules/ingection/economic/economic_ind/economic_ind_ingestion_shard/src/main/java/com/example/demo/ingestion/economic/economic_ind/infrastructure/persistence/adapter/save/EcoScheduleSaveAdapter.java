package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.save;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.infra_shard.persistence.EntityToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EcoScheduleSaveAdapter implements FlushAndSaveEconomicValuePort<EconomicSchedule> {

    private final JpaRepository<EconomicScheduleEntity, Long> repo;
    private final EcoScheduleCache scheduleCache;
    private final DomainToEntity<EconomicSchedule, EconomicScheduleEntity> domainToEntity;
    private final EntityToDomain<EconomicScheduleEntity, EconomicSchedule> entityToDomain;

    @Override
    public void saveAll(List<EconomicSchedule> schedules) {
        List<EconomicScheduleEntity> entities = schedules.stream().map(domainToEntity::toEntity).toList();
        repo.saveAll(entities).forEach(saved ->
            scheduleCache.put(entityToDomain.toDomain(saved), saved.getId())
        );
        flush();
    }

    @Override
    public void flush() {
        repo.flush();
    }
}
