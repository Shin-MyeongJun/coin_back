package com.example.demo.ingestion.economic.economic_ind.application.usecase;


import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndEntity;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public abstract class ScheduledEcoIndService<RAW> implements ScheduledEcoIndUseCase {

    private final LoadRawIndDataPort<RAW> loadRawIndDataPort;
    private final RawToDomain<RAW, EconomicRawIndicator> rawMapper;
    private final DomainToEntity<EconomicRawIndicator,EcoIndEntity> ecoMapper;
    private final DomainToEntity<EconomicIndicatorCode,EcoIndCodeEntity> ecoCodeMapper;
    private final FlushAndSaveEconomicValuePort<EcoIndEntity> indSave;
    private final FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave;

    @Override
    public void process() {
        List<RAW> rawList = loadRawIndDataPort.getRaws();
        List<EconomicRawIndicator> indList =
        rawList.stream().map(raw -> {
            Map<String, String> map = new HashMap<>();
            return rawMapper.toDomain(raw, map);
        }).toList();

        //code save
        codeSave.saveAll(
                indList.stream()
                        .map(ind-> ecoCodeMapper.toEntity(ind.code())
                ).toList());
        //ind save all
        indSave.saveAll(
                indList.stream()
                        .map(ecoMapper::toEntity)
                        .toList());
    }
}
