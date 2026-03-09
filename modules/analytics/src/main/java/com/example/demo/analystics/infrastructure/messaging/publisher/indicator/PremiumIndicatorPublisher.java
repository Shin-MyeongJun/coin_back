package com.example.demo.analystics.infrastructure.messaging.publisher.indicator;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;

import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumIndicatorPublisher implements PublishAnalyticValuePort<PremiumIndicatorMessage> {
    private final KafkaTemplate<String, PremiumIndicatorMessage> kafkaTemplate;

    @Override
    public void publish(PremiumIndicatorMessage pm) {
        kafkaTemplate.send("analytics.premium-indicator", pm);
    }
}