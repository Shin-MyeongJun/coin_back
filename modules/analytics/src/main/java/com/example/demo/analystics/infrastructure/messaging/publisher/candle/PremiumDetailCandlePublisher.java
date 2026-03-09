package com.example.demo.analystics.infrastructure.messaging.publisher.candle;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailCandlePublisher implements PublishAnalyticValuePort<PremiumDetailCandleMessage> {
    private final KafkaTemplate<String, PremiumDetailCandleMessage> kafkaTemplate;

    @Override
    public void publish(PremiumDetailCandleMessage pdm) {
        kafkaTemplate.send("analytics.premium-detail-candle", pdm);
    }
}
