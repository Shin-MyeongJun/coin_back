package com.example.demo.ingestion.exchange.infrastruct.publisher;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.ingestion.exchange.application.port.out.RawPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TickRawPublisher implements RawPublishPort<TickRawMessage> {
    private final KafkaTemplate<String, TickRawMessage> kafkaTemplate;

    @Override
    public void publish(TickRawMessage tickRawMessage) {
        kafkaTemplate.send("ingestion-exchange.tick-raw",tickRawMessage.base() ,tickRawMessage);
    }
}
