package com.example.demo.ingestion.economic.economic_ind.infrastructure.scheduler;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public abstract class EcoIndScheduler {

    private final ScheduledEcoIndUseCase indCase;
    private final ScheduledEcoScheduleUseCase scheduleCase;

    @Scheduled(cron = "0 0 6 * * *")
    public void process() {
        scheduleCase.process();
        indCase.process();
    }
}
