package com.example.demo.alert.infrastructure.messaging.config;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AlertKafkaConsumerConfig {
    private final Environment environment;

    private Map<String, Object> commonConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getRequiredProperty("spring.kafka.bootstrap-servers"));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return config;
    }

    private <MESSAGE> ConcurrentKafkaListenerContainerFactory<String, MESSAGE> factoryFor(Class<MESSAGE> clazz) {
        ConcurrentKafkaListenerContainerFactory<String, MESSAGE> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        JsonDeserializer<MESSAGE> valueDeserializer = new JsonDeserializer<>(clazz, false);
        valueDeserializer.addTrustedPackages("com.example.demo.contracts.message");
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                commonConsumerConfig(),
                new StringDeserializer(),
                valueDeserializer
        ));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TickMessage> tickAlertKafkaListenerContainerFactory() {
        return factoryFor(TickMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumMessage> premiumAlertKafkaListenerContainerFactory() {
        return factoryFor(PremiumMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumDetailMessage> premiumDetailAlertKafkaListenerContainerFactory() {
        return factoryFor(PremiumDetailMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TickIndicatorMessage> tickIndicatorAlertKafkaListenerContainerFactory() {
        return factoryFor(TickIndicatorMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumIndicatorMessage> premiumIndicatorAlertKafkaListenerContainerFactory() {
        return factoryFor(PremiumIndicatorMessage.class);
    }
}
