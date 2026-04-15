package infrastructure.persistence.adapter.read;

import application.port.out.ReadScheduledEcoPort;
import infrastructure.persistence.entity.EconomicScheduleEntity;
import infrastructure.persistence.repo.EconomicScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EconomicScheduleReadAdapter implements ReadScheduledEcoPort {

    private final EconomicScheduleRepository repo;

    @Override
    public List<EconomicScheduleEntity> readPending() {
        return repo.findAllPendingSchedules();
    }
}
