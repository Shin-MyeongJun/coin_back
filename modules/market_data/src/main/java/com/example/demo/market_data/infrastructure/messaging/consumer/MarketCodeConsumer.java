package com.example.demo.market_data.infrastructure.messaging.consumer;

import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.infra_shard.messaging.consumer.KafkaDomainConsumer;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import com.example.demo.market_data.application.port.in.ConsumeMetaSnapUseCase;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShot;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MarketCodeConsumer  extends KafkaDomainConsumer<MarketCodeSnapShot, MarketCodeMessage> {

    private  final ConsumeMetaSnapUseCase<MarketCodeSnapShot> usecase;

    protected MarketCodeConsumer(MessageToDomain<MarketCodeMessage, MarketCodeSnapShot> messageMapper, ConsumeMetaSnapUseCase<MarketCodeSnapShot> usecase) {
        super(messageMapper);
        this.usecase = usecase;
    }


    @KafkaListener(
            topics = "meta-data.market-code",
            groupId = "market-data.market-code.cache",
            containerFactory = "marketCodeKafkaListenerContainerFactory"
    )
    @Override
    protected void onMessage(ConsumerRecord<String, MarketCodeMessage> record) {
           MarketCodeMessage message = parse(record);
           MarketCodeSnapShot snap = toDomain(message);
           usecase.consumeMeta(snap);
    }
}
