package com.example.demo.infra_heartbeat.infrastrcuture.message.config;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HealthKafkaConsumerConfig {

    private final String bootstrapServers;
    private final String consumerGroupId;

    public HealthKafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            GetInstanceIdPort idGetter
    ) {
        this.bootstrapServers = bootstrapServers;
        this.consumerGroupId = "infra-heartbeat-" + idGetter.get();
    }

    private Map<String, Object> commonConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return config;
    }

    private <MESSAGE> ConsumerFactory<String, MESSAGE> consumerFactoryFor(Class<MESSAGE> clazz) {
        JsonDeserializer<MESSAGE> valueDeserializer = new JsonDeserializer<>(clazz, false);
        valueDeserializer.addTrustedPackages("com.example.demo.contracts.message.health");

        return new DefaultKafkaConsumerFactory<>(
                commonConsumerConfig(),
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConsumerFactory<String, HeartBeatMessage> heartbeatConsumerFactory() {
        return consumerFactoryFor(HeartBeatMessage.class);
    }

    @Bean
    public ConsumerFactory<String, HealthChangeMessage> healthChangeConsumerFactory() {
        return consumerFactoryFor(HealthChangeMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, HeartBeatMessage> heartbeatKafkaListenerContainerFactory(
            ConsumerFactory<String, HeartBeatMessage> heartbeatConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, HeartBeatMessage>();
        factory.setConsumerFactory(heartbeatConsumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, HealthChangeMessage> healthChangeKafkaListenerContainerFactory(
            ConsumerFactory<String, HealthChangeMessage> healthChangeConsumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, HealthChangeMessage>();
        factory.setConsumerFactory(healthChangeConsumerFactory);
        return factory;
    }
}
