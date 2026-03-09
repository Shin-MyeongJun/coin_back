package com.example.demo.infra_shard.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public abstract class KafkaRecodeConsumer<MESSAGE> {
    protected abstract void onMessage(ConsumerRecord<String,MESSAGE> record);
}
