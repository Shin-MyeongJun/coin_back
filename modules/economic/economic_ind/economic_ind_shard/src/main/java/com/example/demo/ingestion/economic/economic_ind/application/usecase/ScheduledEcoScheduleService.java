package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class ScheduledEcoScheduleService<RAW> implements ScheduledEcoScheduleUseCase {

    private final LoadRawIndDataPort<RAW> loadRawIndDataPort;
    private final RawToDomain<RAW, EconomicSchedule> rawMapper;
    private final FlushAndSaveEconomicValuePort<EconomicSchedule> scheduleSave;
    private final FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;

    @Override
    public void process() {
        List<EconomicSchedule> indList = loadRawIndDataPort.getRaws()
                .stream()
                .filter(Objects::nonNull)
                .map(raw -> rawMapper.toDomain(raw, new HashMap<>()))
                .filter(Objects::nonNull)
                .toList();

        codeSave.saveAll(indList.stream().map(EconomicSchedule::getCode).toList());
        scheduleSave.saveAll(indList);
    }
}
