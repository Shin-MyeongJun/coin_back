package com.example.demo.market_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.market_data.application.port.in.ConsumeRawValueUseCase;
import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.infrastructure.messaging.mapper.raw.FxMessageMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FxConsumer extends KafkaDomainConsumer<Fx, FxMessage> {

    private final ConsumeRawValueUseCase<Fx> useCase;

    protected FxConsumer(FxMessageMapper messageMapper, ConsumeRawValueUseCase<Fx> useCase) {
        super(messageMapper);
        this.useCase = useCase;
    }

    @Override
    @KafkaListener(
            topics = "ingestion-fx.fx",
            groupId = "market-data.fx.cache",
            containerFactory = "fxKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, FxMessage> record) {
        FxMessage message = record.value();
        useCase.consumeValue(toDomain(message));
    }
}
