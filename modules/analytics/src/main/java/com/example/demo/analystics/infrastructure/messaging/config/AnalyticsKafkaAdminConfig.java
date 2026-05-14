package com.example.demo.analystics.infrastructure.messaging.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class AnalyticsKafkaAdminConfig {
    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic tickCandleTopic() {
        return TopicBuilder.name("analytics.tick-candle")
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "86400000",
                        "segment.ms", "3600000",
                        "retention.bytes", "5368709120"
                ))
                .build();
    }

    @Bean
    public NewTopic tickIndicatorTopic() {
        return TopicBuilder.name("analytics.tick-indicator")
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "86400000",
                        "segment.ms", "3600000",
                        "retention.bytes", "5368709120"
                ))
                .build();
    }

    @Bean
    public NewTopic premiumCandleTopic() {
        return TopicBuilder.name("analytics.premium-candle")
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "86400000",
                        "segment.ms", "3600000",
                        "retention.bytes", "5368709120"
                ))
                .build();
    }

    @Bean
    public NewTopic premiumIndicatorTopic() {
        return TopicBuilder.name("analytics.premium-indicator")
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "86400000",
                        "segment.ms", "3600000",
                        "retention.bytes", "5368709120"
                ))
                .build();
    }

    @Bean
    public NewTopic premiumDetailCandleTopic() {
        return TopicBuilder.name("analytics.premium-detail-candle")
                .partitions(3)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "86400000",
                        "segment.ms", "3600000",
                        "retention.bytes", "5368709120"
                ))
                .build();
    }
}