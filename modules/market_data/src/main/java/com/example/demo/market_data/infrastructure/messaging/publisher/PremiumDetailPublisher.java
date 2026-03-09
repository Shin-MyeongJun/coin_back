package com.example.demo.market_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.market_data.application.port.out.PublishPriceValuePort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailPublisher implements PublishPriceValuePort<PremiumDetailMessage> {
    private final KafkaTemplate<String, PremiumDetailMessage> kafkaTemplate;

    public void publish(PremiumDetailMessage m){
        kafkaTemplate.send("market-data.premium-detail",m.extractKey(),m);
    }
}
