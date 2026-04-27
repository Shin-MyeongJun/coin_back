package application.usecase;

import application.port.out.FlushAndSaveEconomicValuePort;
import application.port.out.ReadEcoIndCodePort;
import application.port.out.ReadScheduledEcoPort;
import com.example.demo.infra_shard.persistence.EntityMapping;
import domain.EconomicIndicatorCode;
import domain.EconomicSchedule;
import infrastructure.dto.ReleaseDateDto;
import infrastructure.persistence.entity.EcoIndCodeEntity;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class FredSyncScheduleService extends SyncScheduleService<ReleaseDateDto> {
    public FredSyncScheduleService(ReadScheduledEcoPort readScheduledPort, ReadEcoIndCodePort readEcoIndCodePort, FlushAndSaveEconomicValuePort<EcoIndCodeEntity> writeEcoIndCodePort, FlushAndSaveEconomicValuePort<EconomicScheduleEntity> writeScheduleSPort, EntityMapping<EconomicSchedule, EconomicScheduleEntity> scheduleMapper, EntityMapping<EconomicIndicatorCode, EcoIndCodeEntity> codeMapper) {
        super(readScheduledPort, readEcoIndCodePort, writeEcoIndCodePort, writeScheduleSPort, scheduleMapper, codeMapper);
    }
}
