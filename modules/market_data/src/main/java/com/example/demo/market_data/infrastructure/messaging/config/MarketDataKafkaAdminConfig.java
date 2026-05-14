package com.example.demo.market_data.infrastructure.messaging.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class MarketDataKafkaAdminConfig {
    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic tickTopic() {
        return TopicBuilder.name("market-data.tick")
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "21600000",
                        "segment.ms", "3600000",
                        "retention.bytes", "32212254720"
                ))
                .build();
    }

    @Bean
    public NewTopic premiumTopic() {
        return TopicBuilder.name("market-data.premium")
                .partitions(15)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "21600000",
                        "segment.ms", "3600000",
                        "retention.bytes", "10737418240"
                ))
                .build();
    }

    @Bean
    public NewTopic premiumDetailTopic() {
        return TopicBuilder.name("market-data.premium-detail")
                .partitions(15)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "10800000",
                        "segment.ms", "3600000",
                        "retention.bytes", "16106127360"
                ))
                .build();
    }
}
