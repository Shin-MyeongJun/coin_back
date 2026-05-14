package com.example.demo.ingestion.exchange.infrastruct.config;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.contracts.message.raw.TickRawMessage;
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
public class ExchangeKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonProducerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        return config;
    }

    // ===== TickRawMessage용 =====
    @Bean
    public ProducerFactory<String, TickRawMessage> tickRawProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TickRawMessage> tickRawKafkaTemplate() {
        return new KafkaTemplate<>(tickRawProducerFactory());
    }

    // ===== MarketCodeRawMessage용 =====
    @Bean
    public ProducerFactory<String, MarketCodeRawMessage> marketCodeRawProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, MarketCodeRawMessage> marketCodeRawKafkaTemplate() {
        return new KafkaTemplate<>(marketCodeRawProducerFactory());
    }
}
