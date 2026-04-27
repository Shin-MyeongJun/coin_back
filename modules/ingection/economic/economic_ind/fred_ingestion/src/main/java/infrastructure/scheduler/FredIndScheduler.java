package infrastructure.scheduler;

import application.port.in.ScheduledEcoIndUseCase;
import application.port.in.ScheduledEcoScheduleUseCase;
import org.springframework.stereotype.Component;

@Component
public class FredIndScheduler extends EcoIndScheduler {
    public FredIndScheduler(ScheduledEcoIndUseCase indCase, ScheduledEcoScheduleUseCase scheduleCase) {
        super(indCase, scheduleCase);
    }
}
