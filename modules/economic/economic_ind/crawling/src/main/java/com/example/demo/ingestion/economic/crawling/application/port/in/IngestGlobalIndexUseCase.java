package com.example.demo.ingestion.economic.crawling.application.port.in;

public interface IngestGlobalIndexUseCase {
    void ingest();

    interface ForYahoo extends IngestGlobalIndexUseCase {}
    interface ForInvesting extends IngestGlobalIndexUseCase {}
}
