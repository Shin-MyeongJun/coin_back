package com.example.demo.meta_data.infrastructure.messaging.publisher;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.meta_data.application.port.out.PublishMetaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePublisher implements PublishMetaPort<ExchangeMessage> {

    private final KafkaTemplate<String, ExchangeMessage> kafkaTemplate;

    @Override
    public void publish(ExchangeMessage exchangeMessage) {
        kafkaTemplate.send("meta-data.exchange",exchangeMessage);
    }
}
