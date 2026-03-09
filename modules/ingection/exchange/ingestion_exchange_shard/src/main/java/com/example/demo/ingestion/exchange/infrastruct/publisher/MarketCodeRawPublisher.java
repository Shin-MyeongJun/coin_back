package com.example.demo.ingestion.exchange.infrastruct.publisher;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.ingestion.exchange.application.port.out.RawPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarketCodeRawPublisher implements RawPublishPort<MarketCodeRawMessage> {
    private final KafkaTemplate<String, MarketCodeRawMessage> kafkaTemplate;


    @Override
    public void publish(MarketCodeRawMessage marketCodeRawMessage) {
        kafkaTemplate.send("ingestion-exchange.market-code-raw", marketCodeRawMessage);
    }
}
