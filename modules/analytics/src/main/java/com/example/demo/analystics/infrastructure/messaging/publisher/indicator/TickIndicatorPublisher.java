package com.example.demo.analystics.infrastructure.messaging.publisher.indicator;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickIndicatorPublisher implements PublishAnalyticValuePort<TickIndicatorMessage> {
    private final KafkaTemplate<String, TickIndicatorMessage> kafkaTemplate;

    @Override
    public void publish(TickIndicatorMessage pm) {
        kafkaTemplate.send("analytics.tick-indicator", pm);
    }
}