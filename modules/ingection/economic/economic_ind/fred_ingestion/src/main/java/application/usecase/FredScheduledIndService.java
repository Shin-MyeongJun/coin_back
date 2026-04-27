package application.usecase;

import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.EconomicRawIndicator;
import infrastructure.dto.FredObservationResultDto;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EcoIndEntity;
import org.springframework.stereotype.Component;

@Component
public class FredScheduledIndService extends ScheduledEcoIndService<FredObservationResultDto> {
    public FredScheduledIndService(LoadRawIndDataPort<FredObservationResultDto> loadRawIndDataPort, RawToDomain<FredObservationResultDto, EconomicRawIndicator> rawMapper, DomainToEntity<EconomicRawIndicator, EcoIndEntity> ecoMapper, DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper, FlushAndSaveEconomicValuePort<EcoIndEntity> indSave, FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave) {
        super(loadRawIndDataPort, rawMapper, ecoMapper, ecoCodeMapper, indSave, codeSave);
    }
}
