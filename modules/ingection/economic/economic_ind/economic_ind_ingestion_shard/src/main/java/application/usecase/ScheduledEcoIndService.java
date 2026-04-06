package application.usecase;


import application.port.in.ScheduledEcoIndUseCase;
import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.EconomicRawIndicator;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EcoIndEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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
        List<RAW> rawList = loadRawIndDataPort.getRaw();
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
                        .map(ind-> ecoMapper.toEntity(ind))
                        .toList());
    }
}
