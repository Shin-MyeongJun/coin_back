package com.example.demo.analystics.infrastructure.messaging.publisher.candle;

import com.example.demo.analystics.application.port.out.PublishAnalyticValuePort;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickCandlePublisher implements PublishAnalyticValuePort<TickCandleMessage> {
    private final KafkaTemplate<String, TickCandleMessage> kafkaTemplate;

    @Override
    public void publish(TickCandleMessage pm) {
        kafkaTemplate.send("analytics.tick-candle", pm);
    }
}
