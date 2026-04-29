package com.example.demo.economic.crawling.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.economic.EconomicIndicatorMessage;
import com.example.demo.economic.crawling.application.port.out.PublishEcoIndPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EcoIndKafkaPublisher implements PublishEcoIndPort {

    private static final String TOPIC = "economic.indicator";
    private static final String SOURCE = "crawling";

    private final KafkaTemplate<String, EconomicIndicatorMessage> kafkaTemplate;

    @Override
    public void publish(EconomicRawIndicator ind) {
        EconomicIndicatorMessage message = new EconomicIndicatorMessage(
                ind.code().indicatorCode(),
                ind.code().type(),
                ind.code().country(),
                ind.value().value(),
                ind.value().observationDate(),
                ind.value().releaseDate(),
                ind.value().timestamp(),
                SOURCE
        );
        kafkaTemplate.send(TOPIC, ind.code().indicatorCode(), message);
        log.debug("Published economic indicator: {}", ind.code().indicatorCode());
    }
}
