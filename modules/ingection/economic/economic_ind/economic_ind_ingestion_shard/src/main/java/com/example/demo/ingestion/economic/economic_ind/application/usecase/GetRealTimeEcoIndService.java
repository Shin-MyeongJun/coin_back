package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.GetRealTimeEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class GetRealTimeEcoIndService<RAW> implements GetRealTimeEcoIndUseCase {

    private final LoadRawIndDataPort<RAW> loadRawIndDataPort;
    private final RawToDomain<RAW, EconomicRawIndicator> rawMapper;
    private final DomainToEntity<EconomicRawIndicator, EcoIndEntity> ecoMapper;
    private final DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper;
    private final FlushAndSaveEconomicValuePort<EcoIndEntity> indSave;
    private final FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave;

    @Override
    public void process(String target) {
        RAW raw =loadRawIndDataPort.getRaw(target);
        EconomicRawIndicator ind =rawMapper.toDomain(raw,null);
        codeSave.saveAll(List.of(ecoCodeMapper.toEntity(ind.code())) );
        indSave.saveAll(List.of(ecoMapper.toEntity(ind)) );
    }
}
