package com.example.demo.economic.crawling.infrastructure.scheduler;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.scheduler.EcoIndScheduler;
import org.springframework.stereotype.Component;

@Component
public class CrawlingEcoIndScheduler extends EcoIndScheduler {

    public CrawlingEcoIndScheduler(ScheduledEcoIndUseCase indCase, ScheduledEcoScheduleUseCase scheduleCase) {
        super(indCase, scheduleCase);
    }
}
