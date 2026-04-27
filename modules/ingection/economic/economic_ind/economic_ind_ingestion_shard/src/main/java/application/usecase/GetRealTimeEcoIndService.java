package application.usecase;

import application.port.in.GetRealTimeEcoIndUseCase;
import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.EconomicRawIndicator;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EcoIndEntity;
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
