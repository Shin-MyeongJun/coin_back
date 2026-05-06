package com.example.demo.api.stream.consumer;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TickStreamConsumer {

    private final MarketDataStream marketDataStream;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "market-data.tick",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            containerFactory = "streamKafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            TickMessage message = objectMapper.readValue(record.value(), TickMessage.class);
            marketDataStream.tickSink.tryEmitNext(message);
        } catch (Exception e) {
            log.warn("Failed to deserialize TickMessage: {}", e.getMessage());
        }
    }
}
