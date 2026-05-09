package com.example.demo.market_data.infrastructure.messaging.config;

import com.example.demo.contracts.message.chunk.message.ExchangeSnapshotChunkMessage;
import com.example.demo.contracts.message.chunk.message.MarketCodeSnapshotChunkMessage;
import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.contracts.message.raw.TickRawMessage;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MarketDataKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonConsumerConfig(){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(
            Class<T> clazz) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(clazz, false);
        valueDeserializer.addTrustedPackages(
                "com.example.demo.contracts.message",
                "com.example.demo.contracts.message.fx",
                "com.example.demo.contracts.message.tick"
        );


        factory.setConsumerFactory(
                new DefaultKafkaConsumerFactory<>(
                        commonConsumerConfig(),
                        new StringDeserializer(),
                        valueDeserializer
                )
        );
        return factory;
    }




    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TickRawMessage> tickRawKafkaListenerContainerFactory() {
        // groupId는 토픽/용도별로 적절히
        var factory =factoryFor(TickRawMessage.class);
        factory.setConcurrency(10);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FxMessage> fxKafkaListenerContainerFactory() {
        return factoryFor(FxMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExchangeMessage> exchangeKafkaListenerContainerFactory() {
        return factoryFor(ExchangeMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MarketCodeMessage> marketCodeKafkaListenerContainerFactory() {
        return factoryFor(MarketCodeMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MarketCodeSnapshotChunkMessage> marketCodeSnapshotChunkKafkaListenerContainerFactory() {
        return factoryFor(MarketCodeSnapshotChunkMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExchangeSnapshotChunkMessage> exchangeSnapshotChunkKafkaListenerContainerFactory() {
        return factoryFor(ExchangeSnapshotChunkMessage.class);
    }
}
