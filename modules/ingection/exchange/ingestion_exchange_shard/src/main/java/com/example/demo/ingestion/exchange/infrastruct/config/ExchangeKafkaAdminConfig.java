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
                .partitions(10)    // 원하는 파티션 개수
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "60000",      // 1분
                        "segment.ms", "10000",        // 10초
                        "retention.bytes", "1048576" // 1MB
                ))
                .build();
    }

    @Bean
    public NewTopic marketCodeRawTopic() {
        return TopicBuilder.name("ingestion-exchange.market-code-raw")
                .partitions(1)    // 원하는 파티션 개수
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "60000",      // 1분
                        "segment.ms", "10000",        // 10초
                        "retention.bytes", "1048576" // 1MB
                ))
                .build();
    }
}
