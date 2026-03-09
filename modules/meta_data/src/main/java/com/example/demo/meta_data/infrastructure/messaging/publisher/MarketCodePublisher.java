package com.example.demo.meta_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarketCodePublisher implements PublishMetaPort<MarketCodeMessage> {

    private final KafkaTemplate<String, MarketCodeMessage> kafkaTemplate;

    @Override
    public void publish(MarketCodeMessage marketCodeMessage) {
        kafkaTemplate.send("meta-data.market-code", marketCodeMessage);
    }
}
