package com.example.demo.ingestion.economic.crawling.infrastructure.scheduler;

import com.example.demo.ingestion.economic.crawling.application.port.in.IngestGlobalIndexUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GlobalIndexPollingScheduler {

    private final IngestGlobalIndexUseCase.ForYahoo yahooIngestUseCase;
    private final IngestGlobalIndexUseCase.ForInvesting investingIngestUseCase;

    @Scheduled(fixedRateString = "${crawling.yahoo.polling-interval-ms:2000}")
    public void pollYahoo() {
        yahooIngestUseCase.ingest();
    }

    @Scheduled(fixedRateString = "${crawling.investing.polling-interval-ms:5000}")
    public void pollInvesting() {
        investingIngestUseCase.ingest();
    }
}
