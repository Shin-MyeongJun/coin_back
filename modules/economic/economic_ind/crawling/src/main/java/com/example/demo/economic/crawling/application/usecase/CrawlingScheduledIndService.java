package com.example.demo.economic.crawling.application.usecase;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import org.springframework.stereotype.Component;

@Component
public class CrawlingScheduledIndService implements ScheduledEcoIndUseCase {

    @Override
    public void process() {
        // Burst polling in BurstEcoIndFetchService handles real-time value detection at each release time.
        // No batch fetch is needed for web-scraping sources.
    }
}
