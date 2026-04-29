package com.example.demo.ingestion.economic.crawling.application.port.out;

import com.example.demo.ingestion.economic.crawling.domain.domain.GlobalIndex;

public interface PublishGlobalIndexPort {
    void publish(GlobalIndex index);
}
