package com.example.demo.economic.crawling.infrastructure.scheduler;


import com.example.demo.ingestion.economic.economic_ind.application.port.in.GetRealTimeEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.scheduler.EcoDynamicIndScheduler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class CrawlingEcoDynamicScheduler extends EcoDynamicIndScheduler {
    public CrawlingEcoDynamicScheduler(TaskScheduler taskScheduler, EcoScheduleCache ecoScheduleCache, GetRealTimeEcoIndUseCase getTask) {
        super(taskScheduler, ecoScheduleCache, getTask);
    }
}