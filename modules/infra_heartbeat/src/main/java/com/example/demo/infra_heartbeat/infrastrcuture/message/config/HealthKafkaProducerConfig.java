package com.example.demo.infra_heartbeat.infrastrcuture.message.config;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.contracts.message.health.HeartBeatMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class HealthKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonProducerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // JsonSerializer 옵션 (type header 제거 등)
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    @Bean
    public ProducerFactory<String, HeartBeatMessage> heartBeatProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, HeartBeatMessage> heartBeatKafkaTemplate(
            ProducerFactory<String, HeartBeatMessage> heartBeatProducerFactory
    ) {
        return new KafkaTemplate<>(heartBeatProducerFactory);
    }

    @Bean
    public ProducerFactory<String, HealthChangeMessage> healthChangeProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, HealthChangeMessage> healthChangeKafkaTemplate(
            ProducerFactory<String, HealthChangeMessage> healthChangeProducerFactory
    ) {
        return new KafkaTemplate<>(healthChangeProducerFactory);
    }
}
