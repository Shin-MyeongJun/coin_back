package com.example.demo.market_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumPublisher implements PublishPriceValuePort<PremiumMessage> {
    private final KafkaTemplate<String, PremiumMessage> kafkaTemplate;

    public void publish(PremiumMessage m){
        kafkaTemplate.send("market-data.premium",m.extractKey(),m);
    }
}
