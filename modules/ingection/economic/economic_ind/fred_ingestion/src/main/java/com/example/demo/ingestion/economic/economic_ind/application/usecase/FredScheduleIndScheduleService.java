package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class FredScheduleIndScheduleService extends ScheduledEcoScheduleService<ReleaseDateDto> {

    public FredScheduleIndScheduleService(LoadRawIndDataPort<ReleaseDateDto> loadRawIndDataPort, RawToDomain<ReleaseDateDto, EconomicSchedule> rawMapper, DomainToEntity<EconomicSchedule, EconomicScheduleEntity> ecoMapper, DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper, FlushAndSaveEconomicValuePort<EconomicScheduleEntity> indSave, FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave) {
        super(loadRawIndDataPort, rawMapper, ecoMapper, ecoCodeMapper, indSave, codeSave);
    }
}
