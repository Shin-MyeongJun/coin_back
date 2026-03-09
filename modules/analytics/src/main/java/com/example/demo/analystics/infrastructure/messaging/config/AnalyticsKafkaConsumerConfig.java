package com.example.demo.analystics.infrastructure.messaging.config;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.contracts.message.price_value.TickMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AnalyticsKafkaConsumerConfig {

    private Map<String, Object> commonConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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
                "com.example.demo.contracts.message"
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
    public ConcurrentKafkaListenerContainerFactory<String, TickMessage> tickKafkaListenerContainerFactory() {
        // groupId는 토픽/용도별로 적절히
        return factoryFor(TickMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumMessage> premiumKafkaListenerContainerFactory() {
        // groupId는 토픽/용도별로 적절히
        return factoryFor(PremiumMessage.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumDetailMessage> premiumDetailKafkaListenerContainerFactory() {
        // groupId는 토픽/용도별로 적절히
        return factoryFor(PremiumDetailMessage.class);
    }

}
