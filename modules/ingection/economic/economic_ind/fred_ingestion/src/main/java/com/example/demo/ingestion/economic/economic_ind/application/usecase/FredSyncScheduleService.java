package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadEcoIndCodePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadScheduledEcoPort;
import com.example.demo.infra_shard.persistence.EntityMapping;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class FredSyncScheduleService extends SyncScheduleService<ReleaseDateDto> {
    public FredSyncScheduleService(ReadScheduledEcoPort readScheduledPort, ReadEcoIndCodePort readEcoIndCodePort, FlushAndSaveEconomicValuePort<EcoIndCodeEntity> writeEcoIndCodePort, FlushAndSaveEconomicValuePort<EconomicScheduleEntity> writeScheduleSPort, EntityMapping<EconomicSchedule, EconomicScheduleEntity> scheduleMapper, EntityMapping<EconomicIndicatorCode, EcoIndCodeEntity> codeMapper) {
        super(readScheduledPort, readEcoIndCodePort, writeEcoIndCodePort, writeScheduleSPort, scheduleMapper, codeMapper);
    }
}
