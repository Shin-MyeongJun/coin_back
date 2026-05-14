package com.example.demo.ingestion.exchange.infrastruct.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class ExchangeKafkaAdminConfig {
    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic tickRawTopic() {
        return TopicBuilder.name("ingestion-exchange.tick-raw")
                .partitions(10)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "3600000",
                        "segment.ms", "1800000",
                        "retention.bytes", "21474836480"
                ))
                .build();
    }

    @Bean
    public NewTopic marketCodeRawTopic() {
        return TopicBuilder.name("ingestion-exchange.market-code-raw")
                .partitions(1)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "compact"
                ))
                .build();
    }
}
