package com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.adapter.read;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadScheduledEcoPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo.EconomicScheduleRepository;
import com.example.demo.infra_shard.persistence.EntityToDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EconomicScheduleReadAdapter implements ReadScheduledEcoPort {

    private final EconomicScheduleRepository repo;
    private final EntityToDomain<EconomicScheduleEntity, EconomicSchedule> mapper;

    @Override
    public List<EconomicSchedule> readPending() {
        return repo.findAllPendingSchedules().stream().map(mapper::toDomain).toList();
    }
}
