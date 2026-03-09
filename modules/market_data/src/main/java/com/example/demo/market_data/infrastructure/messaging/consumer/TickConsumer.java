package com.example.demo.market_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.application.port.in.ConsumeRawValueUseCase;
import com.example.demo.market_data.domain.domain.Tick;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public  class TickConsumer extends KafkaDomainConsumer<Tick, TickRawMessage> {

    private final ConsumeRawValueUseCase<Tick> useCase;

    protected TickConsumer(MessageToDomain<TickRawMessage, Tick> messageMapper, ConsumeRawValueUseCase<Tick> useCase) {
        super(messageMapper);
        this.useCase = useCase;
    }


    @Override
    @KafkaListener(
            topics = "ingestion-exchange.tick-raw",
            groupId = "market-data.tick-raw.handle",
            containerFactory = "tickRawKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, TickRawMessage> record) {
       TickRawMessage message =  parse(record);
       Tick tick = toDomain(message);
       if(tick == null)
           return;
       useCase.consumeValue(tick);
    }
}
