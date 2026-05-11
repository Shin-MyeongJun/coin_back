package com.example.demo.market_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.application.port.in.ConsumeMetaSnapUseCase;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShot;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ExchangeConsumer extends KafkaDomainConsumer<ExchangeSnapShot, ExchangeMessage> {

    private  final ConsumeMetaSnapUseCase<ExchangeSnapShot> usecase;

    protected ExchangeConsumer(MessageToDomain<ExchangeMessage, ExchangeSnapShot> messageMapper, ConsumeMetaSnapUseCase<ExchangeSnapShot> usecase) {
        super(messageMapper);
        this.usecase = usecase;
    }


    @Override
    @KafkaListener(
            topics = "meta-data.exchange",
            containerFactory = "exchangeKafkaListenerContainerFactory"
    )
    protected void onMessage(ConsumerRecord<String, ExchangeMessage> record) {
        ExchangeSnapShot snapShot = toDomain(parse(record));
        usecase.consumeMeta(snapShot);
    }
}
