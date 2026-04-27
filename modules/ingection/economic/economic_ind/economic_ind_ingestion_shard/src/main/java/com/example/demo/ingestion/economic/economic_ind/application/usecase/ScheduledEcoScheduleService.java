package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class ScheduledEcoScheduleService<RAW> implements ScheduledEcoScheduleUseCase {

    private final LoadRawIndDataPort<RAW> loadRawIndDataPort;
    private final RawToDomain<RAW, EconomicSchedule> rawMapper;
    private final DomainToEntity<EconomicSchedule, EconomicScheduleEntity> ecoMapper;
    private final DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper;
    private final FlushAndSaveEconomicValuePort<EconomicScheduleEntity> indSave;
    private final FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave;

    @Override
    public void process() {
        List<RAW> rawList = loadRawIndDataPort.getRaws();
        List<EconomicSchedule> indList =
                rawList.stream().map(raw -> {
                    Map<String, String> map = new HashMap<>();
                    return rawMapper.toDomain(raw, map);
                }).toList();

        //code save
        codeSave.saveAll(
                indList.stream()
                        .map(ind-> ecoCodeMapper.toEntity(ind.getCode())
                        ).toList());
        //ind save all
        indSave.saveAll(
                indList.stream()
                        .map(ecoMapper::toEntity)
                        .toList());
    }


}
