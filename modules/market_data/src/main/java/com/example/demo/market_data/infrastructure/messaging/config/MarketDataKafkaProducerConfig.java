package com.example.demo.market_data.infrastructure.messaging.config;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.contracts.message.price_value.TickMessage;
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
public class MarketDataKafkaProducerConfig {

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

    // ===== TickRawMessage용 =====
    @Bean
    public ProducerFactory<String, TickMessage> tickProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TickMessage> tickKafkaTemplate() {
        return new KafkaTemplate<>(tickProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PremiumMessage> premiumProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PremiumMessage> premiumKafkaTemplate() {
        return new KafkaTemplate<>(premiumProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PremiumDetailMessage> premiumDetailProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PremiumDetailMessage> premiumDetailKafkaTemplate() {
        return new KafkaTemplate<>(premiumDetailProducerFactory());
    }


}
