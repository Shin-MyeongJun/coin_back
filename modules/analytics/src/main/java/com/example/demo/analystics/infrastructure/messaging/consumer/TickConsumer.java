package com.example.demo.analystics.infrastructure.messaging.consumer;


import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.application.port.in.TickAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaRecodeConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TickConsumer extends KafkaRecodeConsumer<TickMessage> {

    private final ParsingPriceValueUseCase<TickMessage, TickKey, BigDecimal> parser;
    private final TickAnalyticsUseCase useCase;

    @Override
    @KafkaListener(
            topics = "market-data.tick",
            groupId = "analytic.tick.calculating",
            containerFactory = "tickKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, TickMessage> record) {
        TickMessage message = record.value();
        TickKey key = parser.parseKey(message);
        BigDecimal value = parser.parseValue(message);
        useCase.onData(record.partition(),key,value);
    }
}