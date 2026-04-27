package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.GetRealTimeEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class GetRealTimeEcoIndService<RAW> implements GetRealTimeEcoIndUseCase {

    private final LoadRawIndDataPort<RAW> loadRawIndDataPort;
    private final RawToDomain<RAW, EconomicRawIndicator> rawMapper;
    private final FlushAndSaveEconomicValuePort<EconomicRawIndicator> indSave;
    private final FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;

    @Override
    public void process(String target) {
        RAW raw = loadRawIndDataPort.getRaw(target);
        EconomicRawIndicator ind = rawMapper.toDomain(raw, null);
        codeSave.saveAll(List.of(ind.code()));
        indSave.saveAll(List.of(ind));
    }
}
