package com.example.demo.meta_data.infrastructure.messaging.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class MetaDataKafkaAdminConfig {
    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic exchangeTopic() {
        return TopicBuilder.name("meta-data.exchange")
                .partitions(1)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "compact"
                ))
                .build();
    }

    @Bean
    public NewTopic marketCodeTopic() {
        return TopicBuilder.name("meta-data.market-code")
                .partitions(1)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "compact"
                ))
                .build();
    }

}
