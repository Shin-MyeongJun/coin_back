package application.usecase;

import application.port.in.SyncScheduleUseCase;
import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import application.port.out.ReadEcoIndCodePort;
import application.port.out.ReadScheduledEcoPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.EntityMapping;
import domain.EconomicIndicatorCode;
import domain.EconomicSchedule;
import infrastructure.cache.EcoIndCodeCache;
import infrastructure.cache.EcoScheduleCache;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class SyncScheduleService<RAW> implements SyncScheduleUseCase {

    private final ReadScheduledEcoPort readScheduledPort;
    private final ReadEcoIndCodePort readEcoIndCodePort;
    private final FlushAndSaveEconomicValuePort<EcoIndCodeEntity> writeEcoIndCodePort;
    private final FlushAndSaveEconomicValuePort<EconomicScheduleEntity> writeScheduleSPort;

    private final EntityMapping<EconomicSchedule,EconomicScheduleEntity > scheduleMapper;
    private final EntityMapping<EconomicIndicatorCode,EcoIndCodeEntity> codeMapper;

    LoadRawIndDataPort<RAW> getters;
    RawToDomain<RAW,EconomicSchedule> rawToDomain;

    EcoIndCodeCache codeCache;
    EcoScheduleCache scheduleCache;

    @Override
    public void sync() {

        // 1. 우선 외부 데이터 조회
        List<EconomicSchedule> fetchedSchedules = getters.getRaw()
                .stream().map(raw -> rawToDomain.toDomain(raw,null))
                .toList();

        // 2. DB 작성 (Schedule)
        List<EconomicScheduleEntity> scheduleEntities = fetchedSchedules.stream()
                .map(scheduleMapper::toEntity)
                .toList();
        writeScheduleSPort.saveAll(scheduleEntities);

        // 3. DB 작성 (Code)
        List<EcoIndCodeEntity> indCodeEntities = fetchedSchedules.stream()
                .map(sh -> codeMapper.toEntity(sh.getCode()))
                .toList();
        writeEcoIndCodePort.saveAll(indCodeEntities);

        // 4. DB 재조회 (최신 상태)
        List<EconomicScheduleEntity> latestScheduleEntities = readScheduledPort.readPending();
        List<EcoIndCodeEntity> latestCodeEntities = readEcoIndCodePort.readAll();


        // 5-1. Code 캐시 업데이트 (Entity ID 추출 -> Map 변환)
        Map<Long, EconomicIndicatorCode> latestCodes = latestCodeEntities.stream()
                .collect(Collectors.toMap(
                        EcoIndCodeEntity::getId,  // Key: Entity의 ID
                        codeMapper::toDomain      // Value: 변환된 Domain 객체
                ));
        codeCache.put(latestCodes);

        Map<Long, EconomicSchedule> latestSchedules = latestScheduleEntities.stream()
                .collect(Collectors.toMap(
                        EconomicScheduleEntity::getId, // Key: Entity의 ID
                        scheduleMapper::toDomain       // Value: 변환된 Domain 객체
                ));
        scheduleCache.put(latestSchedules);
    }
}
