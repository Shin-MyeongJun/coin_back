package com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging;

import com.example.demo.ingestion.economic.economic_ind.application.port.out.PublishEcoIndPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import org.springframework.stereotype.Component;

@Component
public class EcoIndPublisher implements PublishEcoIndPort {
    @Override
    public void publish(EconomicRawIndicator ind) {

    }
}
