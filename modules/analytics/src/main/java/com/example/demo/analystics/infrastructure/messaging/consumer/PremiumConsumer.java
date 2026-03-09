package com.example.demo.analystics.infrastructure.messaging.consumer;

import com.example.demo.analystics.application.port.in.ConsumeMarketDataUseCase;
import com.example.demo.analystics.application.port.in.ParsingPriceValueUseCase;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaRecodeConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PremiumConsumer extends KafkaRecodeConsumer<PremiumMessage> {

    private final ParsingPriceValueUseCase<PremiumMessage, PremiumKey, BigDecimal> parser;
    private final ConsumeMarketDataUseCase<PremiumKey, BigDecimal> useCase;

    @Override
    @KafkaListener(
            topics = "market-data.premium",
            groupId = "analytic.premium.calculating",
            containerFactory = "premiumKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, PremiumMessage> record) {
        PremiumMessage message = record.value();
        PremiumKey key = parser.parseKey(message);
        BigDecimal value = parser.parseValue(message);
        useCase.process(record.partition(),key,value);
    }
}
