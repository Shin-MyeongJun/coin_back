package com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging;

import com.example.demo.contracts.message.economic.EconomicIndicatorMessage;
import com.example.demo.infra_shard.messaging.mapper.DomainToMessage;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.PublishEcoIndPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoIndPublisher implements PublishEcoIndPort {

    private final KafkaTemplate<String, EconomicIndicatorMessage> kafkaTemplate;
    private final DomainToMessage<EconomicRawIndicator, EconomicIndicatorMessage> mapper;

    @Override
    public void publish(EconomicRawIndicator ind) {
        EconomicIndicatorMessage message = mapper.toMessage(ind);
        kafkaTemplate.send(EconomicTopics.INDICATOR, message.indicatorCode(), message);
    }
}
