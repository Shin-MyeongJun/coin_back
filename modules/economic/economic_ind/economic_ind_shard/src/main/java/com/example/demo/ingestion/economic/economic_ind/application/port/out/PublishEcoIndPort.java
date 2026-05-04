package com.example.demo.ingestion.economic.economic_ind.application.port.out;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;

public interface PublishEcoIndPort {
    void publish(EconomicRawIndicator ind);
}
