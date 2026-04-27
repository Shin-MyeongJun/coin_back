package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadEcoIndCodePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadScheduledEcoPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.dto.ReleaseDateDto;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import org.springframework.stereotype.Component;

@Component
public class FredSyncScheduleService extends SyncScheduleService<ReleaseDateDto> {
    public FredSyncScheduleService(
            ReadScheduledEcoPort readScheduledPort,
            ReadEcoIndCodePort readEcoIndCodePort,
            FlushAndSaveEconomicValuePort<EconomicIndicatorCode> writeEcoIndCodePort,
            FlushAndSaveEconomicValuePort<EconomicSchedule> writeScheduleSPort,
            LoadRawIndDataPort<ReleaseDateDto> getters,
            RawToDomain<ReleaseDateDto, EconomicSchedule> rawToDomain) {
        super(readScheduledPort, readEcoIndCodePort, writeEcoIndCodePort, writeScheduleSPort, getters, rawToDomain);
    }
}
