package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import org.springframework.stereotype.Component;

@Component
public class FredScheduledIndService extends ScheduledEcoIndService<FredObservationResultDto> {
    public FredScheduledIndService(LoadRawIndDataPort<FredObservationResultDto> loadRawIndDataPort, RawToDomain<FredObservationResultDto, EconomicRawIndicator> rawMapper, DomainToEntity<EconomicRawIndicator, EcoIndEntity> ecoMapper, DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper, FlushAndSaveEconomicValuePort<EcoIndEntity> indSave, FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave) {
        super(loadRawIndDataPort, rawMapper, ecoMapper, ecoCodeMapper, indSave, codeSave);
    }
}
