package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class ScheduledEcoIndService<RAW> implements ScheduledEcoIndUseCase {

    private final LoadRawIndDataPort<RAW> loadRawIndDataPort;
    private final RawToDomain<RAW, EconomicRawIndicator> rawMapper;
    private final FlushAndSaveEconomicValuePort<EconomicRawIndicator> indSave;
    private final FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;

    @Override
    public void process() {
        List<EconomicRawIndicator> indList = loadRawIndDataPort.getRaws()
                .stream()
                .filter(Objects::nonNull)
                .map(raw -> rawMapper.toDomain(raw, new HashMap<>()))
                .filter(Objects::nonNull)
                .toList();

        codeSave.saveAll(indList.stream().map(EconomicRawIndicator::code).toList());
        indSave.saveAll(indList);
    }
}
