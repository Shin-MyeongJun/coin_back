package com.example.demo.meta_data.infrastructure.messaging.config;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MetaDataKafkaConsumerConfig {

    private Map<String, Object> commonConsumerConfig(){
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // JSON으로 바꿀 경우 변경
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MarketCodeRawMessage> marketRawKafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, MarketCodeRawMessage>();
        JsonDeserializer<MarketCodeRawMessage> valueDeserializer = new JsonDeserializer<>(MarketCodeRawMessage.class, false);
        valueDeserializer.addTrustedPackages(
                "com.example.demo.contracts.message"
        );

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                commonConsumerConfig(),
                new StringDeserializer(),
                valueDeserializer
        ));
        return factory;
    }


}
