package com.example.demo.alert.infrastructure.messaging.config;

import com.example.demo.contracts.message.alert.AlertFiringMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AlertKafkaProducerConfig {
    private final Environment environment;

    private Map<String, Object> commonProducerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getRequiredProperty("spring.kafka.bootstrap-servers"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        return config;
    }

    @Bean
    public ProducerFactory<String, AlertFiringMessage> alertFiringProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, AlertFiringMessage> alertFiringKafkaTemplate() {
        return new KafkaTemplate<>(alertFiringProducerFactory());
    }
}
