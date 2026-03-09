package com.example.demo.analystics.infrastructure.messaging.config;

import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AnalyticsKafkaProducerConfig {
    private Map<String, Object> commonProducerConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // JsonSerializer 옵션 (type header 제거 등)
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return config;
    }

    //Tick
    @Bean
    public ProducerFactory<String, TickCandleMessage> tickCandleProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TickCandleMessage> tickCandleKafkaTemplate() {
        return new KafkaTemplate<>(tickCandleProducerFactory());
    }

    @Bean
    public ProducerFactory<String, TickIndicatorMessage> tickIndicatorProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, TickIndicatorMessage> tickIndicatorKafkaTemplate() {
        return new KafkaTemplate<>(tickIndicatorProducerFactory());
    }
    //Premium
    @Bean
    public ProducerFactory<String, PremiumCandleMessage> premiumCandleProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PremiumCandleMessage> premiumCandleKafkaTemplate() {
        return new KafkaTemplate<>(premiumCandleProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PremiumIndicatorMessage> premiumIndicatorProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PremiumIndicatorMessage> premiumIndicatorKafkaTemplate() {
        return new KafkaTemplate<>(premiumIndicatorProducerFactory());
    }
    //Premium-Detail
    @Bean
    public ProducerFactory<String, PremiumDetailCandleMessage> premiumDetailCandleProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PremiumDetailCandleMessage> premiumDetailCandleKafkaTemplate() {
        return new KafkaTemplate<>(premiumDetailCandleProducerFactory());
    }


}
