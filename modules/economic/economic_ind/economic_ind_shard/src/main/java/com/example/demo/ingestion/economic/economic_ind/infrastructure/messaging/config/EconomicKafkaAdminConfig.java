package com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging.config;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.messaging.EconomicTopics;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class EconomicKafkaAdminConfig {

    @Bean
    public KafkaAdmin economicKafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> configs = Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic economicIndicatorTopic() {
        return TopicBuilder.name(EconomicTopics.INDICATOR)
                .partitions(1)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "604800000",
                        "segment.ms", "86400000",
                        "retention.bytes", "1073741824"
                ))
                .build();
    }
}
