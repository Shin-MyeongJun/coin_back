package com.example.demo.analystics.infrastructure.messaging.consumer;

import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.application.port.in.PremiumDetailAnalyticsUseCase;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaRecodeConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PremiumDetailConsumer extends KafkaRecodeConsumer<PremiumDetailMessage> {

    private final ParsingPriceValueUseCase<PremiumDetailMessage, PremiumKey, PremiumDetailValue> parser;
    private final PremiumDetailAnalyticsUseCase useCase;

    @Override
    @KafkaListener(
            topics = "market-data.premium-detail",
            groupId = "analytic.premium-detail.calculating",
            containerFactory = "premiumDetailKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, PremiumDetailMessage> record) {
        PremiumDetailMessage message = record.value();
        PremiumKey key = parser.parseKey(message);
        PremiumDetailValue value = parser.parseValue(message);
        useCase.onData(record.partition(),key, value);
    }
}