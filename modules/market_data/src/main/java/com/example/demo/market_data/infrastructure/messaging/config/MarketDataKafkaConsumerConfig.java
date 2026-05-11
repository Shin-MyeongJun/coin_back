package com.example.demo.market_data.infrastructure.messaging.config;

import com.example.demo.contracts.message.chunk.message.ExchangeSnapshotChunkMessage;
import com.example.demo.contracts.message.chunk.message.MarketCodeSnapshotChunkMessage;
import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.contracts.message.meta.ExchangeMessage;
import com.example.demo.contracts.message.meta.MarketCodeMessage;
import com.example.demo.contracts.message.raw.TickRawMessage;
import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
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

    private final String bootstrapServers;
    private final String instanceId;

    public MarketDataKafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            GetInstanceIdPort instanceIdProvider
    ) {
        this.bootstrapServers = bootstrapServers;
        this.instanceId = instanceIdProvider.get();
    }

    private Map<String, Object> commonConsumerConfig(){
        return commonConsumerConfig("statistics-group", "earliest");
    }

    private Map<String, Object> commonConsumerConfig(String groupId, String autoOffsetReset){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return config;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(
            Class<T> clazz) {
        return factoryFor(clazz, "statistics-group");
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(
            Class<T> clazz,
            String groupId) {
        return factoryFor(clazz, groupId, "earliest");
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(
            Class<T> clazz,
            String groupId,
            String autoOffsetReset) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(clazz, false);
        valueDeserializer.addTrustedPackages(
                "com.example.demo.contracts.message",
                "com.example.demo.contracts.message.fx",
                "com.example.demo.contracts.message.tick"
        );


        factory.setConsumerFactory(
                new DefaultKafkaConsumerFactory<>(
                        commonConsumerConfig(groupId, autoOffsetReset),
                        new StringDeserializer(),
                        valueDeserializer
                )
        );
        return factory;
    }

    private String broadcastGroupId(String cacheName) {
        return "market-data." + cacheName + ".cache-" + instanceId;
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
        return factoryFor(FxMessage.class, broadcastGroupId("fx"), "latest");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ExchangeMessage> exchangeKafkaListenerContainerFactory() {
        return factoryFor(ExchangeMessage.class, broadcastGroupId("exchange"), "latest");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MarketCodeMessage> marketCodeKafkaListenerContainerFactory() {
        return factoryFor(MarketCodeMessage.class, broadcastGroupId("market-code"), "latest");
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
