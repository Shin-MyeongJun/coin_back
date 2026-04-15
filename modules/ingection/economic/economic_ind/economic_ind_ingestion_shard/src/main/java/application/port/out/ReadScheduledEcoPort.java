package application.port.out;

import infrastructure.persistence.entity.EconomicScheduleEntity;

import java.util.List;

public interface ReadScheduledEcoPort {
    List<EconomicScheduleEntity> readPending();
}
