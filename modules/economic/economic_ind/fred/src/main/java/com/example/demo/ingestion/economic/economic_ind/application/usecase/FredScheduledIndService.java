package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.FredObservationResultDto;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import org.springframework.stereotype.Component;

@Component
public class FredScheduledIndService extends ScheduledEcoIndService<FredObservationResultDto> {
    public FredScheduledIndService(
            LoadRawIndDataPort<FredObservationResultDto> loadRawIndDataPort,
            RawToDomain<FredObservationResultDto, EconomicRawIndicator> rawMapper,
            FlushAndSaveEconomicValuePort<EconomicRawIndicator> indSave,
            FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave) {
        super(loadRawIndDataPort, rawMapper, indSave, codeSave);
    }
}
