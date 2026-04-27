package infrastructure.scheduler;

import application.port.in.ScheduledEcoIndUseCase;
import application.port.in.ScheduledEcoScheduleUseCase;
import application.port.out.SchedulingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public abstract class EcoIndScheduler implements SchedulingPort {

    private final ScheduledEcoIndUseCase indCase;
    private final ScheduledEcoScheduleUseCase scheduleCase;

    @Override
    @Scheduled(cron = "0 0 6 * * *")
    public void process() {
        scheduleCase.process();
        indCase.process();
    }
}
