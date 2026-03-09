package com.example.demo.infra_shard.messaging.consumer;

import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public abstract class KafkaDomainConsumer<DOMAIN ,MESSAGE> extends KafkaRecodeConsumer<MESSAGE> {
    private final MessageToDomain<MESSAGE,DOMAIN> messageMapper;

    protected KafkaDomainConsumer(MessageToDomain<MESSAGE,DOMAIN> messageMapper) {
        this.messageMapper = messageMapper;
    }
    protected final DOMAIN toDomain(MESSAGE msg){
        return messageMapper.toDomain(msg);
    }
    protected MESSAGE parse(ConsumerRecord<String,MESSAGE> record){
        return record.value();
    }


}
