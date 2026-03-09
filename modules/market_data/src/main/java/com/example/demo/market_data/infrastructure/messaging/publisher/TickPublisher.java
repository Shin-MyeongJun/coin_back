package com.example.demo.market_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickPublisher implements PublishPriceValuePort<TickMessage> {
    private final KafkaTemplate<String, TickMessage> kafkaTemplate;

    public void publish(TickMessage m){
        kafkaTemplate.send("market-data.tick",m.extractKey(),m);
    }
}
