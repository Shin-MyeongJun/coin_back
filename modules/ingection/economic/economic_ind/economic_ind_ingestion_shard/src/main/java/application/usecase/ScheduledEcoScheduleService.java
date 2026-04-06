package application.usecase;

import application.port.in.ScheduledEcoScheduleUseCase;
import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.EconomicSchedule;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class ScheduledEcoScheduleService<RAW> implements ScheduledEcoScheduleUseCase {

    LoadRawIndDataPort<RAW> loadRawIndDataPort;
    RawToDomain<RAW, EconomicSchedule> rawMapper;
    DomainToEntity<EconomicSchedule, EconomicScheduleEntity> ecoMapper;
    DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper;
    FlushAndSaveEconomicValuePort<EconomicScheduleEntity> indSave;
    FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave;

    @Override
    public void process() {
        List<RAW> rawList = loadRawIndDataPort.getRaw();
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
                        .map(ind-> ecoMapper.toEntity(ind))
                        .toList());
    }


}
