package application.usecase;

import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.LoadRawIndDataPort;
import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.infra_shard.persistence.DomainToEntity;
import domain.EconomicIndicatorCode;
import domain.EconomicSchedule;
import infrastructure.dto.ReleaseDateDto;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class FredScheduleIndScheduleService extends ScheduledEcoScheduleService<ReleaseDateDto> {

    public FredScheduleIndScheduleService(LoadRawIndDataPort<ReleaseDateDto> loadRawIndDataPort, RawToDomain<ReleaseDateDto, EconomicSchedule> rawMapper, DomainToEntity<EconomicSchedule, EconomicScheduleEntity> ecoMapper, DomainToEntity<EconomicIndicatorCode, EcoIndCodeEntity> ecoCodeMapper, FlushAndSaveEconomicValuePort<EconomicScheduleEntity> indSave, FlushAndSaveEconomicValuePort<EcoIndCodeEntity> codeSave) {
        super(loadRawIndDataPort, rawMapper, ecoMapper, ecoCodeMapper, indSave, codeSave);
    }
}
