package com.example.demo.analystics.infrastructure.messaging.publisher.candle;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumCandlePublisher implements PublishAnalyticValuePort<PremiumCandleMessage> {
    private final KafkaTemplate<String, PremiumCandleMessage> kafkaTemplate;

    @Override
    public void publish(PremiumCandleMessage pm) {
        kafkaTemplate.send("analytics.premium-candle", pm);
    }
}
