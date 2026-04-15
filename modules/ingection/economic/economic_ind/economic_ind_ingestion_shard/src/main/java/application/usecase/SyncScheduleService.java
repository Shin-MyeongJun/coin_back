package application.usecase;

import application.port.in.SyncScheduleUseCase;
import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import application.port.out.ReadEcoIndCodePort;
import application.port.out.ReadScheduledEcoPort;
import com.example.demo.infra_shard.persistence.EntityMapping;
import domain.EconomicIndicatorCode;
import domain.EconomicSchedule;
import infrastructure.cache.EcoIndCodeCache;
import infrastructure.cache.EcoScheduleCache;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class SyncScheduleService implements SyncScheduleUseCase {

    private final ReadScheduledEcoPort readScheduledPort;
    private final ReadEcoIndCodePort readEcoIndCodePort;
    private final FlushAndSaveEconomicValuePort<EcoIndCodeEntity> writeEcoIndCodePort;
    private final FlushAndSaveEconomicValuePort<EconomicScheduleEntity> writeScheduleSPort;

    private final EntityMapping<EconomicSchedule,EconomicScheduleEntity > scheduleMapper;
    private final EntityMapping<EconomicIndicatorCode,EcoIndCodeEntity> codeMapper;

    List<LoadRawIndDataPort<EconomicSchedule>> getters;

    EcoIndCodeCache codeCache;
    EcoScheduleCache scheduleCache;

    @Override
    public void sync() {

        //우선 조회
        List<EconomicSchedule> fetchedSchedules = getters.stream()
                .flatMap(getter -> getter.getRaw().stream())
                .toList();

        //db 작성
        List<EconomicScheduleEntity> scheduleEntities = fetchedSchedules.stream()
                .map(scheduleMapper::toEntity)
                .toList();
        writeScheduleSPort.saveAll(scheduleEntities);

        List<EcoIndCodeEntity> indCodeEntities = fetchedSchedules.stream()
                .map(sh -> codeMapper.toEntity(sh.getCode())).toList();
        writeEcoIndCodePort.saveAll(indCodeEntities);
        //db 조회
        List<EconomicScheduleEntity> latestScheduleEntities = readScheduledPort.readPending();
        List<EcoIndCodeEntity> latestCodeEntities = readEcoIndCodePort.readAll();

        //캐시 업데이트
        List<EconomicSchedule> latestSchedules = latestScheduleEntities.stream()
                .map(scheduleMapper::toDomain)
                .toList();
        scheduleCache;

        // 지표 코드 캐시 업데이트
        List<EconomicIndicatorCode> latestCodes = latestCodeEntities.stream()
                .map(codeMapper::toDomain)
                .toList();
        codeCache;

    }
}
