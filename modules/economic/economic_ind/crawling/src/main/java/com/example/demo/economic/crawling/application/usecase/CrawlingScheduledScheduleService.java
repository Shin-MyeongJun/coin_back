package com.example.demo.economic.crawling.application.usecase;

import com.example.demo.infra_shard.persistence.EntityToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.DynamicSchedulingPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoIndCodeCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EcoIndCodeEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.entity.EconomicScheduleEntity;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo.EcoIndCodeRepository;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.persistence.repo.EconomicScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingScheduledScheduleService implements ScheduledEcoScheduleUseCase {

    private final EcoIndCodeRepository codeRepository;
    private final EconomicScheduleRepository scheduleRepository;
    private final EntityToDomain<EcoIndCodeEntity, EconomicIndicatorCode> codeMapper;
    private final EntityToDomain<EconomicScheduleEntity, EconomicSchedule> scheduleMapper;
    private final EcoIndCodeCache ecoIndCodeCache;
    private final EcoScheduleCache ecoScheduleCache;
    private final DynamicSchedulingPort dynamicSchedulingPort;

    @Override
    public void process() {
        Map<Long, EconomicIndicatorCode> codeMap = codeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(EcoIndCodeEntity::getId, codeMapper::toDomain));
        ecoIndCodeCache.put(codeMap);

        Map<Long, EconomicSchedule> scheduleMap = scheduleRepository.findAllPendingSchedules()
                .stream()
                .collect(Collectors.toMap(EconomicScheduleEntity::getId, scheduleMapper::toDomain));
        ecoScheduleCache.put(scheduleMap);

        dynamicSchedulingPort.adjustSchedule();
        log.info("Cache refreshed: {} codes, {} pending schedules loaded", codeMap.size(), scheduleMap.size());
    }
}
