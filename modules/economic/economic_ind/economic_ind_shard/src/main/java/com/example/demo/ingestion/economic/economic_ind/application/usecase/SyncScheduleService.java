package com.example.demo.ingestion.economic.economic_ind.application.usecase;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.SyncScheduleUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.LoadRawIndSchedulePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadEcoIndCodePort;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.ReadScheduledEcoPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class SyncScheduleService<RAW> implements SyncScheduleUseCase {

    private final ReadScheduledEcoPort readScheduledPort;
    private final ReadEcoIndCodePort readEcoIndCodePort;
    private final FlushAndSaveEconomicValuePort<EconomicIndicatorCode> writeEcoIndCodePort;
    private final FlushAndSaveEconomicValuePort<EconomicSchedule> writeScheduleSPort;
    private final LoadRawIndSchedulePort<RAW> getters;
    private final RawToDomain<RAW, EconomicSchedule> rawToDomain;

    @Override
    public void sync() {
        // 1. 외부 데이터 조회 → 도메인 변환
        List<EconomicSchedule> fetchedSchedules = getters.getRawSchedules()
                .stream()
                .map(raw -> rawToDomain.toDomain(raw, null))
                .toList();

        // 2. DB 작성 (Schedule) — 캐시 갱신은 EcoScheduleSaveAdapter 책임
        writeScheduleSPort.saveAll(fetchedSchedules);

        // 3. DB 작성 (Code) — 캐시 갱신은 EcoCodeSaveAdapter 책임
        List<EconomicIndicatorCode> codes = fetchedSchedules.stream()
                .map(EconomicSchedule::getCode)
                .toList();
        writeEcoIndCodePort.saveAll(codes);

        // 4. DB 재조회 (최신 상태)
        List<EconomicSchedule> latestSchedules = readScheduledPort.readPending();
        List<EconomicIndicatorCode> latestCodes = readEcoIndCodePort.readAll();

        // 5. TODO: 전체 캐시 일괄 동기화 로직은 다음 단계에서 (어댑터 책임으로 이관 예정)
    }
}
