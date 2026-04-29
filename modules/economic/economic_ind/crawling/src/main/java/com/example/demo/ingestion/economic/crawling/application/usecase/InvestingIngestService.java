package com.example.demo.ingestion.economic.crawling.application.usecase;

import com.example.demo.ingestion.economic.crawling.application.port.in.IngestGlobalIndexUseCase;
import com.example.demo.ingestion.economic.crawling.application.port.out.FetchGlobalIndexPort;
import com.example.demo.ingestion.economic.crawling.application.port.out.PublishGlobalIndexPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestingIngestService implements IngestGlobalIndexUseCase.ForInvesting {
    private final FetchGlobalIndexPort.ForInvesting fetchPort;
    private final PublishGlobalIndexPort publishPort;

    @Override
    public void ingest() {
        fetchPort.fetchAll().forEach(publishPort::publish);
    }
}
