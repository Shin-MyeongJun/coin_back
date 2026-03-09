package com.example.demo.meta_data.infrastructure.messaging.consumer;


import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaRecodeConsumer;
import com.example.demo.meta_data.application.port.in.ConsumeMetaRawUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketCodeRawConsumer extends KafkaRecodeConsumer<MarketCodeRawMessage> {

    private final ConsumeMetaRawUseCase useCase;

    @Override
    @KafkaListener(
            topics = "ingestion-exchange.market-code-raw",
            groupId = "meta-data.market-code-raw.db-writer",
            containerFactory = "marketRawKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, MarketCodeRawMessage> record) {
        log.info("handle market code raw message: {}", record.value());
        useCase.handle(record.value());
    }
}
