package com.example.demo.ingestion.economic.economic_ind.infrastructure.scheduler;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import org.springframework.stereotype.Component;

@Component
public class FredIndScheduler extends EcoIndScheduler {
    public FredIndScheduler(ScheduledEcoIndUseCase indCase, ScheduledEcoScheduleUseCase scheduleCase) {
        super(indCase, scheduleCase);
    }
}
