package com.example.demo.infra_heartbeat.infrastrcuture.message.config;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.contracts.message.health.HeartBeatMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HealthKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonConsumerConfig(){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // JSON으로 바꿀 경우 변경
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(
            Class<T> clazz) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(clazz, false);
        valueDeserializer.addTrustedPackages(
                "com.example.demo.contracts.message.health"
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
    public ConcurrentKafkaListenerContainerFactory<String, HeartBeatMessage> heartbeatKafkaListenerContainerFactory() {
        return factoryFor(HeartBeatMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, HealthChangeMessage> healthChangeKafkaListenerContainerFactory() {
        return factoryFor(HealthChangeMessage.class);
    }
}
