package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndSchedulePort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import org.springframework.stereotype.Component;

@Component
public class FredScheduleIndScheduleService extends ScheduledEcoScheduleService<ReleaseDateDto> {
    public FredScheduleIndScheduleService(
            LoadRawIndSchedulePort<ReleaseDateDto> loadRawIndDataPort,
            RawToDomain<ReleaseDateDto, EconomicSchedule> rawMapper,
            FlushAndSaveEconomicValuePort<EconomicSchedule> scheduleSave,
            FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave) {
        super(loadRawIndDataPort, rawMapper, scheduleSave, codeSave);
    }
}
